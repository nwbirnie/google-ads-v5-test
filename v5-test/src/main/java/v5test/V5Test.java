package v5test;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v5.services.GenerateKeywordIdeaResult;
import com.google.ads.googleads.v5.services.GenerateKeywordIdeasRequest;
import com.google.ads.googleads.v5.services.GenerateKeywordIdeasRequest.Builder;
import com.google.ads.googleads.v5.services.GoogleAdsVersion;
import com.google.ads.googleads.v5.services.KeywordPlanIdeaServiceClient;
import com.google.ads.googleads.v5.services.KeywordPlanIdeaServiceClient.GenerateKeywordIdeasPagedResponse;
import com.google.ads.googleads.v5.services.UrlSeed;
import com.google.protobuf.StringValue;

public class V5Test implements Runnable {

	final GoogleAdsVersion googleAdsClient;

	V5Test() {
		googleAdsClient = setupGoogleAdsClient();
	}

	public void run() {
		System.out.println("starting...");
		final String customerId = System.getenv("GOOGLE_ADS_ACCT_ID");
		final UrlSeed urlSeed = UrlSeed.newBuilder().setUrl(StringValue.of("https://wordsimade.wordpress.com/2019/08/22/giants-among-us/")).build();
		final Builder builder = GenerateKeywordIdeasRequest.newBuilder().setCustomerId(customerId).setUrlSeed(urlSeed);
		final GenerateKeywordIdeasRequest request = builder.build();
		final KeywordPlanIdeaServiceClient keywordPlanIdeaServiceClient = googleAdsClient.createKeywordPlanIdeaServiceClient();
		final GenerateKeywordIdeasPagedResponse response = keywordPlanIdeaServiceClient.generateKeywordIdeas(request);

		for (final GenerateKeywordIdeaResult result : response.getPage().getValues()) {
			System.out.println("text:\t\t\t" + result.getText().getValue());
			System.out.println("avg monthly searches:\t" + result.getKeywordIdeaMetrics().getAvgMonthlySearches().getValue());
			System.out.println();
		}
	}

	private GoogleAdsVersion setupGoogleAdsClient() {
		System.out.println("building google ads client");
		try {
			final Map<String, String> propsMap = new HashMap<String, String>();
			// google account email address
			propsMap.put("serviceAccountUser", "SERVICE_ACCOUNT_USER");
			propsMap.put("clientId", "CLIENT_ID");
			propsMap.put("clientSecret", "CLIENT_SECRET");
			propsMap.put("refreshToken", "REFRESH_TOKEN");
			// manager account ID
			propsMap.put("loginCustomerId", "LOGIN_CUSTOMER_ID");
			propsMap.put("developerToken", "DEVELOPER_TOKEN");
			// see https://developers.google.com/google-ads/api/docs/best-practices/partial-failures
			propsMap.put("isPartialFailure", "IS_PARTIAL_FAILURE");

			final Map<String, String> env = System.getenv();
			final Properties props = new Properties();
			for (final Entry<String, String> e : propsMap.entrySet()) {
				final String envKey = "GOOGLE_ADS_" + e.getValue();
				final String value = env.get(envKey);
				if (value == null) {
					throw new Exception("Environment var not defined: " + envKey);
				}
				props.setProperty("api.googleads." + e.getKey(), value);
			}

			return GoogleAdsClient.newBuilder().fromProperties(props).build().getLatestVersion();
		} catch (final Throwable e) {
			System.out.println("failed to build google client");
			System.out.println(e);
			throw new RuntimeException(e);
		}
	}

	public static void main(final String[] args) {
		new V5Test().run();
	}

}
