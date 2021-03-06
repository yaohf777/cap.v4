package com.sap.rc.sample;

import static org.springframework.http.HttpStatus.NO_CONTENT;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.hcp.cf.logging.common.customfields.CustomField;
import com.sap.rc.main.exception.BadRequestException;
import com.sap.rc.main.exception.NotAuthorizedException;
import com.sap.rc.main.exception.NotFoundException;
import com.sap.rc.main.util.Utility;

/*
 * Use a path which does not end with a slash! Otherwise the controller is not reachable when not using the trailing
 * slash in the URL
 */
@RestController
@RequestMapping(path = AdvertisementController.PATH)
@RequestScope // @Scope(WebApplicationContext.SCOPE_REQUEST)
@Validated
public class AdvertisementController {

    public static final String PATH = "/api/v1/ads";
    public static final String PATH_PAGES = PATH + "/pages/";
    public static final int FIRST_PAGE_ID = 0;
    // allows server side optimization e.g. via caching
    public static final int DEFAULT_PAGE_SIZE = 20;

    private static final Marker TECHNICAL = MarkerFactory.getMarker("TECHNICAL");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AdvertisementRepository adRepository;
    private UserServiceClient userServiceClient;
    private StatisticsServiceClient statisticsServiceClient;

    @Inject
    public AdvertisementController(AdvertisementRepository repository, UserServiceClient userServiceClient,
            StatisticsServiceClient statisticsServiceClient) {
        this.adRepository = repository;
        this.userServiceClient = userServiceClient;
        this.statisticsServiceClient = statisticsServiceClient;
    }

    @GetMapping
    // e.g. http://localhost:8080/api/v1/ads
    public ResponseEntity<AdvertisementListDto> advertisements() {
        return advertisementsForPage(FIRST_PAGE_ID);
    }

    @GetMapping("/pages/{pageId}") // not "public"
    // e.g. http://localhost:8080/api/v1/ads/pages/1
    public ResponseEntity<AdvertisementListDto> advertisementsForPage(@PathVariable("pageId") int pageId) {

    	Pageable pageable = PageRequest.of(pageId, DEFAULT_PAGE_SIZE, Sort.by("createdAt").descending());
		Page<Advertisement> page = adRepository.findAll(pageable);
		
        return new ResponseEntity<AdvertisementListDto>(new AdvertisementListDto(page.getContent()),
                Utility.buildLinkHeader(page, PATH_PAGES), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    // e.g. http://localhost:8080/api/v1/ads/1
    // We do not use primitive "long" type here to avoid unnecessary autoboxing
    public AdvertisementDto advertisementById(@PathVariable("id") @Min(0) Long id) {
        MDC.put("endpoint", "GET: " + PATH + "/" + id);

        logger.info("demonstration of custom fields, not part of message",
                CustomField.customField("example-key", "example-value"));
        logger.info("demonstration of custom fields, part of message: {}",
                CustomField.customField("example-key", "example-value"));
        throwIfNonexisting(id);
        statisticsServiceClient.advertisementIsShown(id);
        AdvertisementDto ad = new AdvertisementDto(adRepository.findById(id));
        logger.info("returning: {}", ad);
        return ad;
    }

    /**
     * @RequestBody is bound to the method argument. HttpMessageConverter resolves method argument depending on the
     *              content type.
     */
    @PostMapping
    public ResponseEntity<AdvertisementDto> add(@Valid @RequestBody AdvertisementDto advertisement,
            UriComponentsBuilder uriComponentsBuilder) throws URISyntaxException {
        throwIfIdNotNull(advertisement.getId());

        if (userServiceClient.isPremiumUser("42")) {

            AdvertisementDto savedAdvertisement = new AdvertisementDto(adRepository.save(advertisement.toEntity()));
            logger.trace(TECHNICAL, "created ad with version {}", savedAdvertisement.metadata.version);
            UriComponents uriComponents = uriComponentsBuilder.path(PATH + "/{id}")
                    .buildAndExpand(savedAdvertisement.getId());
            return ResponseEntity.created(new URI(uriComponents.getPath())).body(savedAdvertisement);
        } else {
            String message = "You need to be a premium user to create an advertisement";
            logger.warn(message);
            throw new NotAuthorizedException(message);
        }
    }

    @DeleteMapping
    @ResponseStatus(NO_CONTENT)
    public void deleteAll() {
        adRepository.deleteAll();
    }

    @DeleteMapping("{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteById(@PathVariable("id") Long id) {
        throwIfNonexisting(id);
        adRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public AdvertisementDto update(@PathVariable("id") long id, @RequestBody AdvertisementDto updatedAd) {
        throwIfInconsistent(id, updatedAd.getId());
        throwIfNonexisting(id);
        adRepository.save(updatedAd.toEntity());
        logger.trace(TECHNICAL, "updated ad with version {}", updatedAd.metadata.version);
        return new AdvertisementDto(adRepository.findById(id)); // Note that EntityManager.merge might not update all
                                                               // fields such as createdAt
    }

    private static void throwIfIdNotNull(final Long id) {
        if (id != null && id.intValue() != 0) {
            String message = String
                    .format("Remove 'id' property from request or use PUT method to update resource with id = %d", id);
            throw new BadRequestException(message);
        }
    }

    private void throwIfNonexisting(long id) {
        if (!adRepository.existsById(id)) {
            NotFoundException notFoundException = new NotFoundException(id + " not found");
            logger.trace("Advertisement {} not found",id);
            throw notFoundException;
        }
    }

    private void throwIfInconsistent(Long expected, Long actual) {
        if (!expected.equals(actual)) {
            String message = String.format(
                    "bad request, inconsistent IDs between request and object: request id = %d, object id = %d",
                    expected, actual);
            throw new BadRequestException(message);
        }
    }

    public static class AdvertisementListDto {
        @JsonProperty("value")
        public List<AdvertisementDto> advertisements;

        public AdvertisementListDto(Iterable<Advertisement> ads) {
            this.advertisements = StreamSupport.stream(ads.spliterator(), false).map(AdvertisementDto::new)
                    .collect(Collectors.toList());
        }
    }
}
