package com.sap.rc.sample;

/*@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { WebAppContextConfig.class })
@WebAppConfiguration*/
public class AdvertisementControllerTest {

/*	private static final String LOCATION = "Location";
	private static final String SOME_TITLE = "MyNewAdvertisement";
	private static final String SOME_OTHER_TITLE = "MyOldAdvertisement";

	@Inject
	WebApplicationContext context;

	private MockMvc mockMvc;

	@Inject
	private Filter springSecurityFilterChain;

	private String jwt;

	@Before
	public void setUp() throws Exception {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilter(springSecurityFilterChain).build();

		// compute valid token with Display and Update scopes
		jwt = new JwtGenerator().getTokenForAuthorizationHeader(ResourceSecurityConfig.DISPLAY_SCOPE,
				ResourceSecurityConfig.UPDATE_SCOPE);
	}

	@Test
	public void create() throws Exception {
		mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated())
				.andExpect(header().string(LOCATION, is(not(""))))
				.andExpect(content().contentType(APPLICATION_JSON_UTF8)).andExpect(jsonPath("$.title", is(SOME_TITLE))); // requires
																															// com.jayway.jsonpath:json-path
	}

	@Test
	public void createAndGetByLocation() throws Exception {
		MockHttpServletResponse response = mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated())
				.andReturn().getResponse();

		// check that the returned location is correct
		mockMvc.perform(get(response.getHeader(LOCATION)).header(AUTHORIZATION, jwt)).andExpect(status().isOk())
				.andExpect(jsonPath("$.title", is(SOME_TITLE)));
	}

	@Test
	public void readAll() throws Exception {
		mockMvc.perform(buildDeleteRequest("")).andExpect(status().isNoContent());

		mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated());
		mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated());

		
		 * mockMvc.perform(buildGetRequest("")) .andExpect(status().isOk())
		 * .andExpect(content().contentType(APPLICATION_JSON_UTF8))
		 * .andExpect(jsonPath("$.length()",
		 * is(both(greaterThan(0)).and(lessThan(10)))));
		 
	}

	@Test
	public void readByIdNotFound() throws Exception {
		mockMvc.perform(buildGetRequest("4711")).andExpect(status().isNotFound());
	}

	@Test
	public void readByIdNegative() throws Exception {
		mockMvc.perform(buildGetRequest("-1")).andExpect(status().isBadRequest());
	}

	@Test
	public void createEmptyTitle() throws Exception {
		mockMvc.perform(buildPostRequest(null)).andExpect(status().isBadRequest());
	}

	@Test
	public void createBlancTitle() throws Exception {
		mockMvc.perform(buildPostRequest("")).andExpect(status().isBadRequest());
	}

	@Test
	public void createWithNoContent() throws Exception {
		mockMvc.perform(
				post(AdvertisementController.PATH).contentType(APPLICATION_JSON_UTF8).header(AUTHORIZATION, jwt))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void createWithId() throws Exception {
		AdvertisementDto advertisement = new AdvertisementDto(SOME_TITLE);
		advertisement.setId(4L);

		mockMvc.perform(post(AdvertisementController.PATH).content(toJson(advertisement))
				.contentType(APPLICATION_JSON_UTF8).header(AUTHORIZATION, jwt)).andExpect(status().isBadRequest());
	}

	@Test
	public void readById() throws Exception {
		String id = performPostAndGetId();

		mockMvc.perform(buildGetRequest(id)).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8)).andExpect(jsonPath("$.title", is(SOME_TITLE)));
	}

	@Test
	public void updateNotFound() throws Exception {
		AdvertisementDto advertisement = new AdvertisementDto(SOME_TITLE);
		advertisement.setId(4711L);
		mockMvc.perform(buildPutRequest("4711", advertisement)).andExpect(status().isNotFound());
	}

	@Test
	public void updateById() throws Exception {

		MockHttpServletResponse response = mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated())
				.andReturn().getResponse();

		AdvertisementDto advertisement = convertJsonContent(response, AdvertisementDto.class);
		advertisement.setTitle(SOME_OTHER_TITLE);
		String id = getIdFromLocation(response.getHeader(LOCATION));

		mockMvc.perform(buildPutRequest(id, advertisement)).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.title", is(SOME_OTHER_TITLE)));
	}

	@Test
	public void updateByNotMatchingId() throws Exception {

		MockHttpServletResponse response = mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated())
				.andReturn().getResponse();

		AdvertisementDto advertisement = convertJsonContent(response, AdvertisementDto.class);

		mockMvc.perform(buildPutRequest("1188", advertisement)).andExpect(status().isBadRequest());
	}

	@Test
	public void deleteNotFound() throws Exception {
		mockMvc.perform(buildDeleteRequest("4711")).andExpect(status().isNotFound());
	}

	@Test
	public void deleteById() throws Exception {
		String id = performPostAndGetId();

		mockMvc.perform(buildDeleteRequest(id)).andExpect(status().isNoContent());

		mockMvc.perform(buildGetRequest(id)).andExpect(status().isNotFound());
	}

	@Test
	public void deleteAll() throws Exception {
		String id = performPostAndGetId();

		mockMvc.perform(buildDeleteRequest("")).andExpect(status().isNoContent());

		mockMvc.perform(buildGetRequest(id)).andExpect(status().isNotFound());
	}

	@Test
	public void doNotReuseIdsOfDeletedItems() throws Exception {
		String id = performPostAndGetId();

		mockMvc.perform(buildDeleteRequest(id)).andExpect(status().isNoContent());

		String idNewAd = performPostAndGetId();

		assertThat(idNewAd, is(not(id)));
	}

	@Test
	public void readAdsFromSeveralPages() throws Exception {
		int adsCount = AdvertisementController.DEFAULT_PAGE_SIZE + 1;

		mockMvc.perform(buildDeleteRequest("")).andExpect(status().isNoContent());

		for (int i = 0; i < adsCount; i++) {
			mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated());
		}

		mockMvc.perform(buildGetByPageRequest(0)).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.value.length()", is(AdvertisementController.DEFAULT_PAGE_SIZE)));

		mockMvc.perform(buildGetByPageRequest(1)).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8)).andExpect(jsonPath("$.value.length()", is(1)));

	}

	@Test
	public void navigatePages() throws Exception {
		int adsCount = (AdvertisementController.DEFAULT_PAGE_SIZE * 2) + 1;

		mockMvc.perform(buildDeleteRequest("")).andExpect(status().isNoContent());

		for (int i = 0; i < adsCount; i++) {
			mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated());
		}

		// get query 
		String linkHeader =	performGetRequest(AdvertisementController.PATH).getHeader(HttpHeaders.LINK);
		assertThat(linkHeader, is("</api/v1/ads/pages/1>; rel=\"next\""));

		// navigate to next 
		String nextLink = extractLinks(linkHeader).get(0); String
		linkHeader2ndPage = performGetRequest(nextLink).getHeader(HttpHeaders.LINK);
		assertThat(linkHeader2ndPage,
				is("</api/v1/ads/pages/0>; rel=\"previous\", </api/v1/ads/pages/2>; rel=\"next\""));

		// navigate to next 
		nextLink = extractLinks(linkHeader2ndPage).get(1); String
		linkHeader3rdPage = performGetRequest(nextLink).getHeader(HttpHeaders.LINK);
		assertThat(linkHeader3rdPage, is("</api/v1/ads/pages/1>; rel=\"previous\""));

		// navigate to previous 
		String previousLink = extractLinks(linkHeader3rdPage).get(0);
		assertThat(performGetRequest(previousLink).getHeader(HttpHeaders.LINK), is(linkHeader2ndPage));

	}

	@Test
	public void createForbiddenWithoutUpdateScope() throws Exception {
		String jwtReadOnly = new JwtGenerator().getTokenForAuthorizationHeader(ResourceSecurityConfig.DISPLAY_SCOPE);
		mockMvc.perform(buildPostRequest(SOME_TITLE, jwtReadOnly)).andExpect(status().isForbidden());
	}

	@Test
	public void readFailsWhenUnauthenticated() throws Exception {
		mockMvc.perform(get(AdvertisementController.PATH)).andExpect(status().isUnauthorized());
	}

	private static List<String> extractLinks(final String linkHeader) {
		final List<String> links = new ArrayList<String>();
		Pattern pattern = Pattern.compile("<(?<link>\\S+)>");
		final Matcher matcher = pattern.matcher(linkHeader);
		while (matcher.find()) {
			links.add(matcher.group("link"));
		}
		return links;
	}

	private MockHttpServletResponse performGetRequest(String path) throws Exception {
		return mockMvc.perform(get(path).header(AUTHORIZATION, jwt)).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8)).andReturn().getResponse();
	}

	private MockHttpServletRequestBuilder buildPostRequest(String adsTitle) throws Exception {
		return buildPostRequest(adsTitle, jwt);
	}

	private MockHttpServletRequestBuilder buildPostRequest(String adsTitle, String jwt) throws Exception {
		AdvertisementDto advertisement = new AdvertisementDto();

		advertisement.setTitle(adsTitle);

		// post the advertisement as a JSON entity in the request body
		return post(AdvertisementController.PATH).content(toJson(advertisement)).contentType(APPLICATION_JSON_UTF8)
				.header(AUTHORIZATION, jwt);
	}

	private String performPostAndGetId() throws Exception {
		MockHttpServletResponse response = mockMvc.perform(buildPostRequest(SOME_TITLE)).andExpect(status().isCreated())
				.andReturn().getResponse();

		return getIdFromLocation(response.getHeader(LOCATION));
	}

	private MockHttpServletRequestBuilder buildGetRequest(String id) throws Exception {
		return get(AdvertisementController.PATH + "/" + id).header(AUTHORIZATION, jwt);
	}

	private MockHttpServletRequestBuilder buildGetByPageRequest(int pageId) throws Exception {
		return get(AdvertisementController.PATH_PAGES + pageId).header(AUTHORIZATION, jwt);

	}

	private MockHttpServletRequestBuilder buildPutRequest(String id, AdvertisementDto advertisement) throws Exception {
		return put(AdvertisementController.PATH + "/" + id).content(toJson(advertisement))
				.contentType(APPLICATION_JSON_UTF8).header(AUTHORIZATION, jwt);
	}

	private MockHttpServletRequestBuilder buildDeleteRequest(String id) throws Exception {
		return delete(AdvertisementController.PATH + "/" + id).header(AUTHORIZATION, jwt);
	}

	private String toJson(Object object) throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(object);
	}

	private String getIdFromLocation(String location) {
		return location.substring(location.lastIndexOf('/') + 1);
	}

	private <T> T convertJsonContent(MockHttpServletResponse response, Class<T> clazz) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		String contentString = response.getContentAsString();
		return objectMapper.readValue(contentString, clazz);
	}*/
}
