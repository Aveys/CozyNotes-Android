package cpe.lesbarbus.cozynotes.authenticator;

import android.accounts.AuthenticatorException;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;

import okio.Buffer;

/**
 * Created by arthurveys on 22/12/15.
 */
public class CozyServerAuthenticate implements ServerAuthenticate {

    public static final MediaType JSON = MediaType.parse("application/x-www-form-urlencoded");
    private OkHttpClient client = new OkHttpClient(); //TODO : create a singleton class of client with generic GET/POST/DELETE

    @Override
    public String userSignIn(final String pass, String url) throws Exception {
        //Create the Auth header (base 64 with owner:pass)
        client.setAuthenticator(new Authenticator() {
            private int mCounter = 0;
            @Override
            public Request authenticate(Proxy proxy, Response response) throws IOException {
                if (mCounter++ > 0) {
                    mCounter = 0;
                    throw new IOException("Invalid login or password");
                }
                String credentials = Credentials.basic("owner",pass);
                return response.request().newBuilder().header("Authorization",credentials).build();
            }

            @Override
            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                return null;
            }
        });

        String passRes=null;
        String formedURL = "https://owner:"+pass+"@"+url+"/device";

        String body = "{\"login\":\""+AccountGeneral.DEVICE_NAME+"\",\"permissions\":{\"File\":{\"description\":\"Synchronise notes\"}}}";
        Log.d("CozyServerAuthenticate","Requested URL : "+formedURL+" \n Request Body : "+body);

        String res = post(formedURL,body);// send the POST request

        Log.d("CozyServerAuthenticate","result="+res);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootnode = null;
        try {
            rootnode = mapper.readValue(res,ObjectNode.class);//try to parse JSON
            passRes=rootnode.get("password").asText();//recover password field
        } catch (IOException e) {
            e.printStackTrace();
        }
        return passRes;

    }

    @Override
    public String userSignoff(final String pass, final String url) throws Exception {
        client.setAuthenticator(new Authenticator() {
            @Override
            public Request authenticate(Proxy proxy, Response response) throws IOException {
                String credentials = Credentials.basic("owner", pass);
                return response.request().newBuilder().header("Authorization", credentials).build();
            }

            @Override
            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                return null;
            }
        });

        String formedURL = "https://owner:"+pass+"@"+url+"/device/"+AccountGeneral.DEVICE_NAME;

        Log.d("CozyServerAuthenticate","Requested URL : "+formedURL);

        String res = delete(formedURL);// send the POST request

        Log.d("CozyServerAuthenticate","result="+res);
       return null;
    }

    /**
     * Send the POST request
     * @param url the url of the API
     * @param json The content of the POST Request
     * @return the response body
     * @throws IOException
     */
    private String post(String url, String json) throws IOException {
        client.networkInterceptors().add(new LoggingInterceptor());
        RequestBody body = RequestBody.create(JSON, json);
        Request req = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("{Content-Type", "application/json}")
                .addHeader("Accept", "*/*")
                .build();
        Response res = client.newCall(req).execute();
        return res.body().string();

    }
    private String delete(String url) throws IOException {
        client.networkInterceptors().add(new LoggingInterceptor());
        Request req = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("Accept", "*/*")
                .build();
        Response res = client.newCall(req).execute();
        return res.body().string();
    }
}
//Interceptor for debug in OkHttp
class LoggingInterceptor implements Interceptor {
    @Override public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long t1 = System.nanoTime();
        Log.i("CozyServerAuthenticate", String.format("Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers()));

        Response response = chain.proceed(request);

        long t2 = System.nanoTime();
        Log.i("CozyServerAuthenticate", String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers()));

        return response;
    }
    private static String bodyToString(final RequestBody request){
        try {
            final RequestBody copy = request;
            final Buffer buffer = new Buffer();
            copy.writeTo(buffer);
            return buffer.readUtf8();
        }
        catch (final IOException e) {
            return "did not work";
        }
    }

}

