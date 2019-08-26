package org.iglooproject.test.elasticsearch;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.StringEntity;
import org.codelibs.elasticsearch.runner.ElasticsearchClusterRunner;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.settings.Settings;
import org.iglooproject.lucene.analysis.french.CoreFrenchMinimalStemFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import plugin.CoreFrenchMinimalStemPlugin;

public class ElasticsearchRunnerPluginIT {

	private static final String GET_METHOD = "GET";

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private String clusterName;

	private ElasticsearchClusterRunner runner;

	private int httpPort = 9000;
	private int tcpPort = 9001;

	@Before
	public void setUpElastic() throws Exception {
		clusterName = "es-minhash-" + System.currentTimeMillis();
		// create runner instance
		runner = new ElasticsearchClusterRunner();
		// create ES nodes
		runner.onBuild(new ElasticsearchClusterRunner.Builder() {
			@Override
			public void build(final int number, final Settings.Builder settingsBuilder) {
				settingsBuilder.put("http.cors.enabled", true);
				settingsBuilder.put("http.cors.allow-origin", "*");
				settingsBuilder.putList("discovery.zen.ping.unicast.hosts", String.format("localhost:%d", tcpPort));
			}
		}).build(ElasticsearchClusterRunner.newConfigs()
				.baseHttpPort(httpPort - 1)
				.baseTransportPort(tcpPort -1)
				.clusterName(clusterName)
				.numOfNode(1)
				.pluginTypes(CoreFrenchMinimalStemPlugin.class.getName()));

		// wait for yellow status
		runner.ensureYellow();
	}

	@After
	public void stopElastic() throws IOException {
		runner.close();
	}

	@Test
	public void plugin() throws ConfigurationException, IOException, URISyntaxException {
		HttpHost host = new HttpHost("localhost", httpPort);
		try (RestClient client = RestClient.builder(host).build()) {
			Map<String, String> params = new HashMap<>();
			params.put("pretty", Boolean.TRUE.toString());

			Map<String, Object> content = new HashMap<>();
			content.put("tokenizer", "whitespace");
			content.put("filter", Collections.singletonList(CoreFrenchMinimalStemFilter.STEMMER_NAME));
			content.put("text", "chapeaux");
			HttpEntity entity = new StringEntity(OBJECT_MAPPER.writeValueAsString(content));
			Request request = new Request(GET_METHOD, "_analyze");
			request.getParameters().putAll(params);
			request.setEntity(entity);
			Response response = client.performRequest(request);
			try (InputStream is = response.getEntity().getContent()) {
				JavaType type = OBJECT_MAPPER.getTypeFactory().constructMapLikeType(HashMap.class, String.class,
						Object.class);
				Map<String, Object> value = OBJECT_MAPPER.readValue(is, type);

				// beware of double-wrapped list in tokens
				// { "tokens": [[ { "token" : "chapeau", ... }]] }
				assertThat(value.get("tokens"))
						// attribute tokens is a list of 1 item that is a map
						.asList().hasSize(1).first().isInstanceOf(Map.class);

				@SuppressWarnings("unchecked")
				List<Map<String, Object>> tokens = (List<Map<String, Object>>) value.get("tokens");
				Map<String, Object> token = tokens.get(0);
				assertThat(token.get("token")).isEqualTo("chapeau");
			}
		}
	}

}
