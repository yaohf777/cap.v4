package com.sap.rc.main.controller;

import static org.springframework.http.HttpStatus.NO_CONTENT;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.sap.rc.main.exception.BadRequestException;
import com.sap.rc.main.exception.NotFoundException;
import com.sap.rc.main.model.Analysis;
import com.sap.rc.main.model.AnalysisRepository;
import com.sap.rc.main.util.Utility;

/*
 * Use a path which does not end with a slash! Otherwise the controller is not reachable when not using the trailing
 * slash in the URL
 */
@RestController
@RequestMapping(path = AnalysisController.PATH)
@RequestScope
@Validated
public class AnalysisController {

	public static final String PATH = "/api/v1/ana";
	public static final String PATH_PAGES = PATH + "/pages/";
	public static final int FIRST_PAGE_ID = 0;
	public static final int DEFAULT_PAGE_SIZE = 20;

	// private static final Marker TECHNICAL = MarkerFactory.getMarker("TECHNICAL");
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private AnalysisRepository repository;
	
	@PersistenceContext
	private EntityManager entityManager;

	@Inject
	public AnalysisController(AnalysisRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	// e.g. http://localhost:8080/api/v1/ana
	public ResponseEntity<AnalysisList> analysises() {
		return analysisForPage(FIRST_PAGE_ID);
	}

	@GetMapping("/pages/{pageId}")
	// e.g. http://localhost:8080/api/v1/ana/pages/1
	public ResponseEntity<AnalysisList> analysisForPage(@PathVariable("pageId") int pageId) {

		Pageable pageable = PageRequest.of(pageId, DEFAULT_PAGE_SIZE, Sort.by("createdAt").descending());
		Page<Analysis> page = repository.findAll(pageable);

		return new ResponseEntity<AnalysisList>(new AnalysisList(page.getContent()),
				Utility.buildLinkHeader(page, PATH_PAGES), HttpStatus.OK);
		
	}
	
	@GetMapping("/name/{nameStr}")
	// e.g. http://localhost:8080/api/v1/ana/name/{nameStr}
	public void analysisByName(@PathVariable("nameStr") String name) {

		// native SQL query
		
		// Query query = entityManager.createNativeQuery("select * from V_ANALYSIS where title like 'A%'");
		// query.setParameter("name", name);
		
		// select from table
		// Query query = entityManager.createNativeQuery("select * from RC_ANALYSIS where title like 'A%'");
		
		// select from CDS view
		// Query query = entityManager.createNativeQuery("select * from RC_ANALYSISVIEW where title like 'A%'");
		
		// set query parameter
		// query.setParameter("name", name);
		
		// List<Object[]> analysis = query.getResultList();
		// analysis = null;
	    
	}
	


	@GetMapping("/{id}")
	// e.g. http://localhost:8080/api/v1/ana/1
	// We do not use primitive "long" type here to avoid unnecessary autoboxing
	public Optional<Analysis> analysisById(@PathVariable("id") @Min(0) Long id) {

		throwIfNonexisting(id);
		return (repository.findById(id));

	}

	/**
	 * @RequestBody is bound to the method argument. HttpMessageConverter resolves
	 *              method argument depending on the content type.
	 */
	@PostMapping
	public ResponseEntity<Analysis> add(@Valid @RequestBody Analysis analysis,
			UriComponentsBuilder uriComponentsBuilder) throws URISyntaxException {
		throwIfIdNotNull(analysis.getId());

		Analysis savedAnalysis = repository.save(analysis);

		UriComponents uriComponents = uriComponentsBuilder.path(PATH + "/{id}").buildAndExpand(savedAnalysis.getId());
		return ResponseEntity.created(new URI(uriComponents.getPath())).body(savedAnalysis);

	}

	@DeleteMapping("{id}")
	@ResponseStatus(NO_CONTENT)
	public void deleteById(@PathVariable("id") Long id) {
		throwIfNonexisting(id);
		repository.deleteById(id);
	}

	@PutMapping("/{id}")
	public Analysis update(@PathVariable("id") long id, @RequestBody Analysis updatedAnalysis) {

		throwIfInconsistent(id, updatedAnalysis.getId());
		throwIfNonexisting(id);
		// Note that EntityManager.merge might not update all fields such as createdAt
		return repository.save(updatedAnalysis);
	}

	private static void throwIfIdNotNull(final Long id) {
		if (id != null && id.intValue() != 0) {
			String message = String
					.format("Remove 'id' property from request or use PUT method to update resource with id = %d", id);
			throw new BadRequestException(message);
		}
	}

	private void throwIfNonexisting(long id) {
		if (!repository.existsById(id)) {
			NotFoundException notFoundException = new NotFoundException(id + " not found");
			logger.trace("Analysis {} not found", id);
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

	public static class AnalysisList {
		@JsonProperty("value")
		public List<Analysis> analysises = new ArrayList<>();

		public AnalysisList(Iterable<Analysis> anas) {
			anas.forEach(analysises::add);
		}
	}

}
