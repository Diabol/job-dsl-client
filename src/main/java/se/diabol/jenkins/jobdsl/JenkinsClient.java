package se.diabol.jenkins.jobdsl;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.StringTokenizer;

public class JenkinsClient {

    private URL url;
    private String username = null;
    private String password = null;

    public JenkinsClient(URL url) {
        this.url = url;
    }

    public JenkinsClient(URL url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }


    private HttpResponse execute(HttpRequest request) throws IOException {
        HttpHost target = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
        HttpClient client;

        if (username != null && password != null) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    new AuthScope(url.getHost(), url.getPort()),
                    new UsernamePasswordCredentials(username, password));

            client = HttpClients.custom()
                    .setDefaultCredentialsProvider(credentialsProvider)

                    .build();
        } else {
            client = HttpClients.custom()
                    .build();

        }
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local
        // auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(target, basicAuth);

        // Add AuthCache to the execution context
        HttpClientContext localContext = HttpClientContext.create();
        localContext.setAuthCache(authCache);

        return client.execute(target, request, localContext);

    }


    public Crumb getApiKey() throws IOException, URISyntaxException, ParseException {

        HttpGet request = new HttpGet(url.getPath() + "/crumbIssuer/api/json");
        HttpResponse response = execute(request);


        HttpEntity entity = response.getEntity();
        if (response.getStatusLine().getStatusCode() != 404) {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(new InputStreamReader(entity.getContent()));

            return new Crumb((String) json.get("crumbRequestField"), (String) json.get("crumb"));

        } else {
            return null;
        }


    }


    public boolean itemExists(String name, boolean isJob) throws IOException, URISyntaxException {
        String requestURL;
        if (isJob) {
            requestURL = (url.getPath() + getFolderPath(name) + "job/" + URLEncoder.encode(getItemName(name)).replace("+", "%20") + "/config.xml");
        } else {
            requestURL = (url.getPath() + getFolderPath(name) + "view/" + URLEncoder.encode(getItemName(name)).replace("+", "%20") + "/config.xml");
        }

        HttpGet request = new HttpGet(requestURL);

        HttpResponse response = execute(request);
        int code = response.getStatusLine().getStatusCode();
        if (code == 404) {
            return false;
        }
        if (code == 200) {
            return true;
        }
        throw new IOException("Error in request " + response.getStatusLine().getStatusCode());

    }

    public void createItem(String name, String config, boolean isJob) throws URISyntaxException, IOException, ParseException {

        String requestURL;
        if (isJob) {
            requestURL = (url.getPath() + getFolderPath(name) + "/createItem?name=" + URLEncoder.encode(getItemName(name)).replace("+", "%20"));
        } else {
            requestURL = (url.getPath() + getFolderPath(name) + "/createView?name=" + URLEncoder.encode(getItemName(name)).replace("+", "%20"));
        }

        HttpPost request = new HttpPost(requestURL);
        request.setEntity(new StringEntity(config, ContentType.APPLICATION_XML));
        Crumb crumb = getApiKey();
        if (crumb != null) {
            request.setHeader(crumb.getField(), crumb.getValue());
        }
        HttpResponse response = execute(request);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Error in request " + response.getStatusLine().getStatusCode());
        }
    }

    public void updateItem(String name, String config, boolean isJob) throws URISyntaxException, IOException, ParseException {

        String uri;
        if (isJob) {
            uri = url.getPath() + getFolderPath(name) + "/job/" + URLEncoder.encode(getItemName(name)).replace("+", "%20") + "/config.xml";
        } else {
            uri = url.getPath() + getFolderPath(name) + "/view/" + URLEncoder.encode(getItemName(name)).replace("+", "%20") + "/config.xml";
        }

        HttpPost request = new HttpPost(uri);
        Crumb crumb = getApiKey();
        if (crumb != null) {
            request.setHeader(crumb.getField(), crumb.getValue());
        }
        request.setEntity(new StringEntity(config, ContentType.APPLICATION_XML));
        HttpResponse response = execute(request);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Error in request " + response.getStatusLine().getStatusCode());
        }
    }

    protected String getFolderPath(String name) throws UnsupportedEncodingException {
        String path = "";
        if (name.contains("/")) {
            StringTokenizer st = new StringTokenizer(name, "/");
            for (int i = 0; i < st.countTokens(); i++) {
                path = path + "/job/" + URLEncoder.encode(st.nextToken(), "UTF-8").replace("+", "%20") + "/";
            }
        } else {
            path = "/";
        }
        return path;
    }

    protected String getItemName(String name) {
        if (name.contains("/")) {
            return name.substring(name.lastIndexOf("/") + 1, name.length());
        } else {
            return name;
        }
    }

    private class Crumb {
        String field;
        String value;

        private Crumb(String field, String value) {
            this.field = field;
            this.value = value;
        }

        public String getField() {
            return field;
        }

        public String getValue() {
            return value;
        }
    }

}
