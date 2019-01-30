package com.seamfix.brprinterapp.service;

import com.seamfix.brprinterapp.model.BioUser;
import com.seamfix.brprinterapp.model.Project;
import com.seamfix.brprinterapp.pojo.rest.GenerateIDCardRequest;
import com.seamfix.brprinterapp.pojo.rest.GenerateIDCardResponse;
import com.seamfix.brprinterapp.pojo.rest.LoginResponse;
import com.seamfix.brprinterapp.pojo.rest.TagResponse;
import io.gsonfire.GsonFireBuilder;
import lombok.extern.log4j.Log4j;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

@Log4j
public class HttpClient {

    private Integer connectTimeOutInSeconds = 60;
    private Integer readTimeoutInSeconds = 60;
    private static final String BASE_ENDPOINT = "/br/";
    private static SSLContext sslContext;

    private final ServiceGenerator serviceGenerator;
    private final String apiBaseUrl;

    public HttpClient(ServiceGenerator serviceGenerator) {
        this.serviceGenerator = serviceGenerator;
        apiBaseUrl = serviceGenerator.constructUrl() + BASE_ENDPOINT;
    }

    /**
     * Some projects require a different destination for syncing records for government compliance
     * purposes, hence this method
     *
     * @param project the project
     * @return the configured httpclient that has the custom connection params
     */
    public static HttpClient getHttpClientForSync(Project project) {
        ServiceGenerator svg = ServiceGenerator.getInstance();

        if (project!= null && StringUtils.isNotBlank(project.getHost())) {
            String customHost = project.getHost();
            log.debug("Using custom url for " + project.getName() + ": " + customHost);
            svg.setCustomUrl(customHost);
        }

        HttpClient hp = new HttpClient(svg);
        //this value is larger than the default 10seconds become sync payload can grow
        hp.readTimeoutInSeconds = 60;

        return hp;
    }

    private Retrofit getRetrofit(boolean enableZip) {
        return new Retrofit.Builder()
                .baseUrl(apiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonFireBuilder()
                                .enableHooks(BioUser.class)
                                .enableHooks(Project.class)
                                .createGsonBuilder().setLenient()
                                .create())
                )
                .client(getOkHttpClient(enableZip))
                .build();
    }
    private OkHttpClient getOkHttpClient(boolean enableZip) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder().connectTimeout(connectTimeOutInSeconds, TimeUnit.SECONDS);

        if (readTimeoutInSeconds != null) {
            builder.readTimeout(readTimeoutInSeconds, TimeUnit.SECONDS);
        }

        builder.sslSocketFactory(getDefaultSSLContext().getSocketFactory(), getTrustManager());

        builder.addInterceptor(chain -> {
            Headers headers = Headers.of(serviceGenerator.getHeaders());
            Request request = chain.request().newBuilder().headers(headers).build();
            return chain.proceed(request);
        });

//        if(enableZip){
//            builder.interceptors().add(new GzipRequestInterceptor());
//        }

        return builder.build();
    }
    private X509TrustManager getTrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                x509Certificates[0].checkValidity();
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                x509Certificates[0].checkValidity();
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }


    private static SSLContext getDefaultSSLContext() {
        try {
            final javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return sslContext;
        } catch (java.security.KeyManagementException | java.security.NoSuchAlgorithmException e) {
            return null;
        }

    }

    private BrPrinterClient getClient(boolean enableZip) {
        return getRetrofit(enableZip).create(BrPrinterClient.class);
    }

    //CALLS

    public LoginResponse userLogin(String email, String password) throws IOException {
        Call<LoginResponse> call = getClient(false).userLogin(email, password);
        retrofit2.Response<LoginResponse> response = call.execute();
        return response.isSuccessful() ? response.body() : new LoginResponse(response.code(), response.message());
    }

    public GenerateIDCardResponse generateIDCard(GenerateIDCardRequest generateIDCardRequest) throws IOException {
        Call<GenerateIDCardResponse> call = getClient(false).generateIDcard(generateIDCardRequest);
        retrofit2.Response<GenerateIDCardResponse> response = call.execute();
        return response.isSuccessful() ? response.body() : new GenerateIDCardResponse(response.code(), response.message());
    }
    public TagResponse tagDevice(String deviceType, String uniqueId) throws IOException {
        Call<TagResponse> call = getClient(false).tagDevice(deviceType, uniqueId);
        retrofit2.Response<TagResponse> response = call.execute();
        return response.isSuccessful() ? response.body() : new TagResponse(response.code(), response.message());
    }


}
