package com.pingan.hf.log4j2.async;

public class HttpClientFactory {

    private static HttpAsyncClient httpAsyncClient = new HttpAsyncClient();

    private HttpClientFactory() {
        
    }

    private static HttpClientFactory httpClientFactory = new HttpClientFactory();

    public static HttpClientFactory getInstance() {
        return httpClientFactory;
    }

    public HttpAsyncClient getHttpAsyncClientPool() {
        return httpAsyncClient;
    }
}



package com.pingan.hf.log4j2.async;

import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;

import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.auth.DigestSchemeFactory;
import org.apache.http.impl.auth.KerberosSchemeFactory;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.ssl.SSLContexts;

public class HttpAsyncClient {
    private static int socketTimeout = 1000; // 设置等待数据超时时间5秒钟 根据业务调整

    private static int connectTimeout = 1000;// 连接超时

    private static int poolSize = 200;// 连接池最大连接数

    private static int maxPerRoute = 100;// 每个主机的并发最多只有1500

    // http代理相关参数
    private String host = "";
    private int port = 0;
    private String username = "";
    private String password = "";

    // 异步httpclient
    private CloseableHttpAsyncClient asyncHttpClient;

    // 异步加代理的httpclient
    private CloseableHttpAsyncClient proxyAsyncHttpClient;

    public HttpAsyncClient() {
        try {
            this.asyncHttpClient = createAsyncClient(false);
            this.proxyAsyncHttpClient = createAsyncClient(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public CloseableHttpAsyncClient createAsyncClient(boolean proxy) throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, MalformedChallengeException, IOReactorException {

        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout).build();

        SSLContext sslcontext = SSLContexts.createDefault();

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);

        // 设置协议http和https对应的处理socket链接工厂的对象
        Registry<SchemeIOSessionStrategy> sessionStrategyRegistry = RegistryBuilder.<SchemeIOSessionStrategy>create().register("http", NoopIOSessionStrategy.INSTANCE).register("https", new SSLIOSessionStrategy(sslcontext)).build();

        // 配置io线程
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(Runtime.getRuntime().availableProcessors()).build();
        // 设置连接池大小
        ConnectingIOReactor ioReactor;
        ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        PoolingNHttpClientConnectionManager conMgr = new PoolingNHttpClientConnectionManager(ioReactor, null, sessionStrategyRegistry, null);

        if (poolSize > 0) {
            conMgr.setMaxTotal(poolSize);
        }

        if (maxPerRoute > 0) {
            conMgr.setDefaultMaxPerRoute(maxPerRoute);
        } else {
            conMgr.setDefaultMaxPerRoute(10);
        }

        ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE).setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8).build();

        Lookup<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create().register(AuthSchemes.BASIC, new BasicSchemeFactory()).register(AuthSchemes.DIGEST, new DigestSchemeFactory()).register(AuthSchemes.NTLM, new NTLMSchemeFactory()).register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory()).register(AuthSchemes.KERBEROS, new KerberosSchemeFactory()).build();
        conMgr.setDefaultConnectionConfig(connectionConfig);

        if (proxy) {
            return HttpAsyncClients.custom().setConnectionManager(conMgr).setDefaultCredentialsProvider(credentialsProvider).setDefaultAuthSchemeRegistry(authSchemeRegistry).setProxy(new HttpHost(host, port)).setDefaultCookieStore(new BasicCookieStore()).setDefaultRequestConfig(requestConfig).build();
        } else {
            return HttpAsyncClients.custom().setConnectionManager(conMgr).setDefaultCredentialsProvider(credentialsProvider).setDefaultAuthSchemeRegistry(authSchemeRegistry).setDefaultCookieStore(new BasicCookieStore()).build();
        }

    }

    public CloseableHttpAsyncClient getAsyncHttpClient() {
        return asyncHttpClient;
    }

    public CloseableHttpAsyncClient getProxyAsyncHttpClient() {
        return proxyAsyncHttpClient;
    }
}

package com.pingan.hf.log4j2.async;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpMainTest {

    private static String utf8Charset = "utf-8";

    public static void main(String[] args) throws Exception {
        List<String> resultList = new ArrayList<String>();

        for (int i = 0; i < 500; i++) {
            testGet(resultList);
        }

        // Thread.currentThread().sleep(1000);
        
        for (String result : resultList) {
            System.out.println(result);
        }

    }

    public static void testGet(List<String> resultList) throws Exception {
        CloseableHttpAsyncClient hc = HttpClientFactory.getInstance().getHttpAsyncClientPool().getAsyncHttpClient();
        hc.start();
        String url = "http://localhost:8696/healthCheck";
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Connection", "close");

        List<BasicNameValuePair> urlParams = new ArrayList<BasicNameValuePair>();
        urlParams.add(new BasicNameValuePair("id", "1"));

        if (null != urlParams) {
            String getUrl = EntityUtils.toString(new UrlEncodedFormEntity(urlParams));
            httpGet.setURI(new URI(httpGet.getURI().toString() + "?" + getUrl));
        }

        hc.execute(httpGet, new FutureCallback<HttpResponse>() {

            @Override
            public void failed(Exception ex) {
                System.out.println("failed");
            }

            @Override
            public void completed(HttpResponse result) {
                try {
                    String repspData = EntityUtils.toString(result.getEntity());
                    resultList.add(repspData);
                } catch (ParseException e) {
                    System.out.println("--- parse error ---");
                } catch (IOException e) {
                    System.out.println("--- io error ---");
                } catch (Exception e) {
                    System.out.println("--- error ---");
                }
            }

            @Override
            public void cancelled() {
                System.out.println("cancle");
            }
        });

    }

    public static void testPost(List<String> resultList) throws Exception {
        CloseableHttpAsyncClient hc = HttpClientFactory.getInstance().getHttpAsyncClientPool().getAsyncHttpClient();

        hc.start();
        HttpPost httpPost = new HttpPost("http://localhost:8696/healthCheck");
        httpPost.setHeader("Connection", "close");

        String postString = "{\"age\":12}";
        if (null != postString) {
            StringEntity entity = new StringEntity(postString.toString(), utf8Charset);
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
        }

        String urlParams = "id=1";
        if (null != urlParams) {
            httpPost.setURI(new URI(httpPost.getURI().toString() + "?" + urlParams));
        }

        hc.execute(httpPost, new FutureCallback<HttpResponse>() {

            @Override
            public void failed(Exception ex) {
                System.out.println("failed");
            }

            @Override
            public void completed(HttpResponse result) {
                try {
                    String repspData = EntityUtils.toString(result.getEntity());
                    resultList.add(repspData);
                } catch (ParseException e) {
                    System.out.println("--- parse error ---");
                } catch (IOException e) {
                    System.out.println("--- io error ---");
                } catch (Exception e) {
                    System.out.println("--- error ---");
                }
            }

            @Override
            public void cancelled() {
                System.out.println("cancle");
            }
        });

    }

    public static void testPost2(List<String> resultList) throws Exception {
        CloseableHttpAsyncClient hc = HttpClientFactory.getInstance().getHttpAsyncClientPool().getAsyncHttpClient();

        hc.start();
        HttpPost httpPost = new HttpPost("http://localhost:8696/healthCheck");
        httpPost.setHeader("Connection", "close");

        List<BasicNameValuePair> postBody = new ArrayList<>();
        postBody.add(new BasicNameValuePair("userName", "root"));
        postBody.add(new BasicNameValuePair("parssWord", "123456"));

        if (null != postBody) {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postBody, "UTF-8");
            httpPost.setEntity(entity);
        }

        List<BasicNameValuePair> urlParams = new ArrayList<BasicNameValuePair>();
        urlParams.add(new BasicNameValuePair("id", "1"));

        if (null != urlParams) {
            String getUrl = EntityUtils.toString(new UrlEncodedFormEntity(urlParams));
            httpPost.setURI(new URI(httpPost.getURI().toString() + "?" + getUrl));
        }

        hc.execute(httpPost, new FutureCallback<HttpResponse>() {
            @Override
            public void failed(Exception ex) {
                System.out.println("failed");
            }

            @Override
            public void completed(HttpResponse result) {
                try {
                    String repspData = EntityUtils.toString(result.getEntity());
                    resultList.add(repspData);
                } catch (ParseException e) {
                    System.out.println("--- parse error ---");
                } catch (IOException e) {
                    System.out.println("--- io error ---");
                } catch (Exception e) {
                    System.out.println("--- error ---");
                }
            }

            @Override
            public void cancelled() {
                System.out.println("cancle");
            }
        });

    }

}




<!-- http clinet -->
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpclient</artifactId>
            <version>4.5.2</version>
        </dependency>
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpmime</artifactId>
            <version>4.5.2</version>
        </dependency>
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpcore</artifactId>
            <version>4.4.4</version>
        </dependency>
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpasyncclient</artifactId>
            <version>4.1.3</version>
        </dependency>

