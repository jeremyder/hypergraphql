package uk.semanticintegration.graphql.sparql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/**
 * Created by szymon on 22/08/2017.
 */
public class SparqlClient {

    private static String queryValuesOfObjectPropertyTemp = "SELECT distinct ?object WHERE {<%s> <%s> ?object . }";
    private static String queryValuesOfDataPropertyTemp = "SELECT distinct (str(?object) as ?value) WHERE {<%1$s> <%2$s> ?object . %3$s }";
    private static String querySubjectsOfObjectPropertyFilterTemp = "SELECT distinct ?subject WHERE { ?subject a <node_x> . ?subject <%1$s> <%2$s> . } ";

    private Model model;

    public SparqlClient(List<String> queries, Map<String, Context> sparqlEndpointsContext) {

        model = ModelFactory.createDefaultModel();

        for (String constructQuery : queries) {

            QueryExecution qexec = QueryExecutionFactory.create(constructQuery, model);

            Context mycxt = qexec.getContext();
            Symbol serviceContext = ARQConstants.allocSymbol("http://jena.hpl.hp.com/Service#", "serviceContext");
            mycxt.put(serviceContext, sparqlEndpointsContext);

            try {
                qexec.execConstruct(model);

            } catch (Exception e) {
                System.out.println(e.fillInStackTrace());
            }
        }

        // model.write(System.out);
    }

    public ResultSet sparqlSelect(String queryString) {

        // System.out.println(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(queryString, model);
        try {
            ResultSet results = qexec.execSelect();
            return results;
        } catch (Exception e) {
            System.out.println("Failed to return the response on query: " + queryString);
            System.out.println(e);
            return null;
        }
    }

    public List<RDFNode> getValuesOfObjectProperty(String subject, String predicate, Map<String, Object> args) {

        String queryString = String.format(queryValuesOfObjectPropertyTemp, subject, predicate);
        ResultSet queryResults = sparqlSelect(queryString);

        if (queryResults != null) {

            List<RDFNode> uriList = new ArrayList<>();

            while (queryResults.hasNext()) {
                QuerySolution nextSol = queryResults.nextSolution();
                RDFNode object = nextSol.get("?object");
                uriList.add(object);
            }
            return uriList;
        }
        return null;
    }

    public RDFNode getValueOfObjectProperty(String subject, String predicate, Map<String, Object> args) {

        String queryString = String.format(queryValuesOfObjectPropertyTemp, subject, predicate);
        ResultSet queryResults = sparqlSelect(queryString);

        if (queryResults != null && queryResults.hasNext()) {

            QuerySolution nextSol = queryResults.nextSolution();
            return nextSol.get("?object");
        }
        return null;
    }

    public List<Object> getValuesOfDataProperty(String subject, String predicate, Map<String, Object> args) {

        String langFilter = "";

        if (args.containsKey("lang")) langFilter = "FILTER (lang(?object)=\"" + args.get("lang").toString() + "\") ";

        String queryString = String.format(queryValuesOfDataPropertyTemp, subject, predicate, langFilter);
        ResultSet queryResults = sparqlSelect(queryString);

        if (queryResults != null) {

            List<Object> valList = new ArrayList<>();

            while (queryResults.hasNext()) {
                QuerySolution nextSol = queryResults.nextSolution();
                String value = nextSol.get("?value").toString();
                valList.add(value);
            }
            return valList;
        } else {
            return null;
        }
    }

    public String getValueOfDataProperty(String subject, String predicate, Map<String, Object> args) {

        String langFilter = "";

        if (args.containsKey("lang")) langFilter = "FILTER (lang(?object)=\"" + args.get("lang").toString() + "\") ";

        String queryString = String.format(queryValuesOfDataPropertyTemp, subject, predicate, langFilter);
        ResultSet queryResults = sparqlSelect(queryString);

        if (queryResults != null && queryResults.hasNext()) {

            QuerySolution nextSol = queryResults.nextSolution();
            String value = nextSol.get("?value").toString();

            return value;
        } else {
            return null;
        }
    }

    public List<RDFNode> getSubjectsOfObjectPropertyFilter(String predicate, String uri, Map<String, Object> args) {

        String queryString = String.format(querySubjectsOfObjectPropertyFilterTemp, predicate, uri);

        ResultSet queryResults = sparqlSelect(queryString);

        if (queryResults != null) {

            List<RDFNode> uriList = new ArrayList<>();

            while (queryResults.hasNext()) {
                QuerySolution nextSol = queryResults.nextSolution();
                RDFNode subject = nextSol.get("?subject");
                uriList.add(subject);
            }
            return uriList;
        } else {
            return null;
        }
    }
}