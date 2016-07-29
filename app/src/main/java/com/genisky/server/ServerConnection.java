package com.genisky.server;

import com.google.gson.Gson;

import java.io.IOException;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.util.EntityUtils;
import cz.msebera.android.httpclient.util.TextUtils;

public class ServerConnection{
    protected final String _token;
    protected final String _server;
    protected final Gson _gson;

    protected ServerConnection(String token, String server){
        _token = token;
        _server = server;
        _gson = new Gson();
    }

    protected  <T> T sendPost(String operation, String entity, final Class<T> classOfT) {
        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(_server + operation);
            if (!TextUtils.isEmpty(_token))
                post.addHeader("clocking-authentication-token", _token);
            StringEntity se = new StringEntity(entity);
            se.setContentType("application/json;charset=utf-8");
            post.addHeader("Accept-Encoding", "utf-8");
            post.setEntity(se);
            final ResponseHandler<T> responseHandler = new ResponseHandler<T>() {
                @Override
                public T handleResponse(final HttpResponse response) throws IOException {
                    int status = response.getStatusLine().getStatusCode();
                    HttpEntity entity = response.getEntity();
                    String body = entity == null ? "" : EntityUtils.toString(entity, "UTF-8");
                    if (status < 200 || status >= 300)
                        throw new ClientProtocolException(body);
                    return _gson.fromJson(body, classOfT);
                }
            };
            return httpclient.execute(post, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected  <T> T sendGet(String operation, final Class<T> classOfT) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(_server + operation);
            if (!TextUtils.isEmpty(_token))
                get.addHeader("clocking-authentication-token", _token);
            get.addHeader("Accept-Encoding", "utf-8");
            ResponseHandler<T> responseHandler = new ResponseHandler<T>() {
                @Override
                public T handleResponse(final HttpResponse response) throws IOException {
                    int status = response.getStatusLine().getStatusCode();
                    HttpEntity entity = response.getEntity();
                    String body = entity == null ? "" : EntityUtils.toString(entity, "UTF-8");
                    if (status < 200 || status >= 300)
                        throw new ClientProtocolException(body);
                    return _gson.fromJson(body, classOfT);
                }
            };
            return httpclient.execute(get, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
