package com.lifeinide.jsonql.hibernate.search.elastic;

import com.lifeinide.jsonql.core.BaseFilterQueryBuilder;
import com.lifeinide.jsonql.core.dto.BasePageableRequest;
import com.lifeinide.jsonql.core.dto.Page;
import com.lifeinide.jsonql.core.filters.*;
import com.lifeinide.jsonql.core.intr.FilterQueryBuilder;
import com.lifeinide.jsonql.core.intr.Pageable;
import com.lifeinide.jsonql.core.intr.QueryFilter;
import com.lifeinide.jsonql.core.intr.Sortable;
import com.lifeinide.jsonql.elasticql.EQLBuilder;
import com.lifeinide.jsonql.elasticql.node.component.EQLComponent;
import com.lifeinide.jsonql.elasticql.node.component.EQLMatchComponent;
import com.lifeinide.jsonql.elasticql.node.component.EQLMatchPhrasePrefixComponent;
import com.lifeinide.jsonql.elasticql.node.query.EQLMatchPhrasePrefixQuery;
import com.lifeinide.jsonql.elasticql.node.query.EQLMatchQuery;
import com.lifeinide.jsonql.hibernate.search.FieldSearchStrategy;
import com.lifeinide.jsonql.hibernate.search.HibernateSearch;
import com.lifeinide.jsonql.hibernate.search.HibernateSearchFilterQueryBuilder;
import org.hibernate.search.elasticsearch.impl.ElasticsearchJsonQueryDescriptor;
import org.hibernate.search.exception.SearchException;
import org.hibernate.search.jpa.FullTextQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Implementation of {@link FilterQueryBuilder} for Hibernate Search using ElasticSearch service.
 *
 * <h2>Searchable fields implementation</h2>
 *
 * {@link HibernateSearchElasticFilterQueryBuilder} can search entities of given type for a text contained in searchable fields with
 * additional filtering support by custom fields. By default we support following types of searchable fields in entities
 * (this behavior is configurable using one of constructors, though).
 *
 * <p>
 * {@link HibernateSearch#FIELD_TEXT} which is appropriate to index <strong>case-insensitively all natural-language fields</strong> and
 * should be analyzed with some analyzer. The indexed entity field has has usually the following definition:
 *
 * <pre>{@code
 * @Field(name = HibernateSearch.FIELD_TEXT)
 * @Analyzer(definition = "standard")
 * protected String myfield;
 * }</pre>
 *
 * Note, that in the example above we use {@code standard} analyzer from ElasticSearch, but there are of course other options.
 * Please see them <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-analyzers.html">here</a>.
 * </p>
 *
 * <p>
 * {@link HibernateSearch#FIELD_ID} which is appropriate to index <strong>case-insensitively all fields tokenized only with
 * whitespaces</strong> and prevents from tokenizing using usual document number separators like slash or dash. So, this kind of field
 * should be analyzed with appropriate analyzer. The indexed entity field has has usually the following definition:
 * <pre>{@code
 * @Field(name = HibernateSearch.FIELD_ID)
 * @Analyzer(definition = "keyword")
 * protected String myfield;
 * }</pre>
 *
 * Note, that in the example above we use {@code keyword} analyzer from ElasticSearch, which is almost perfect for searching by
 * text ids. However this analyzer has a one flaw, which is the lack of {@code lowercase} filter. If you want to apply such a filter,
 * you'd need to create your own custom keyword analyzer in ElasticSearch, as described
 * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-keyword-analyzer.html">here</a>.
 * </p>
 *
 * @see HibernateSearchFilterQueryBuilder More information about Hibernate Search-related filtering.
 * @author Lukasz Frankowski
 */
public class HibernateSearchElasticFilterQueryBuilder<E, P extends Page<E>>
extends BaseFilterQueryBuilder<E, P, FullTextQuery, HibernateSearchElasticQueryBuilderContext<E>, HibernateSearchElasticFilterQueryBuilder<E, P>> {

	public static final Logger logger = LoggerFactory.getLogger(HibernateSearchElasticFilterQueryBuilder.class);
	protected static final EQLBuilder EQL_BUILDER = new EQLBuilder();

	protected HibernateSearchElasticQueryBuilderContext<E> context;

	/**
	 * @param entityClass Use concrete entity class to search for the specific entities, or {@code Object.class} to do a global search.
	 */
	public HibernateSearchElasticFilterQueryBuilder(EntityManager entityManager, Class<E> entityClass, String q) {
		this(entityManager, entityClass, q, defaultSearchFields());
	}

	public HibernateSearchElasticFilterQueryBuilder(EntityManager entityManager, Class<E> entityClass, String q,
											 		Map<String, FieldSearchStrategy> fields) {
		HibernateSearch hibernateSearch = new HibernateSearch(entityManager);
		context = new HibernateSearchElasticQueryBuilderContext<>(q, entityClass, hibernateSearch,
			hibernateSearch.fullTextEntityManager().getSearchFactory().getIndexedTypeDescriptor(entityClass));

		boolean fieldFound = false;
		
		for (Map.Entry<String, FieldSearchStrategy> entry: fields.entrySet()) {
			try {
				context.getEqlBool().withShould(createFieldQuery(entry.getValue(), entry.getKey(), q));
				fieldFound = true;
			} catch (Exception e) {
				// silently, this means that some of our full text fields don't exists in the entity
			}
		}

		if (!fieldFound)
			throw new SearchException(String.format("No fulltext fields found for: %s", entityClass.getSimpleName()));
	}


	@Override
	public HibernateSearchElasticQueryBuilderContext<E> context() {
		return context;
	}

	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, P> add(String field, DateRangeQueryFilter filter) {
		// TODOLF impl HibernateSearchFilterQueryBuilder.add
		return null;
	}

	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, P> add(String field, EntityQueryFilter<?> filter) {
		// TODOLF impl HibernateSearchFilterQueryBuilder.add
		return null;
	}

	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, P> add(String field, ListQueryFilter<? extends QueryFilter> filter) {
		// TODOLF impl HibernateSearchFilterQueryBuilder.add
		return null;
	}

	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, P> add(String field, SingleValueQueryFilter<?> filter) {
		// TODOLF impl HibernateSearchFilterQueryBuilder.add
		return null;
	}

	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, P> add(String field, ValueRangeQueryFilter<? extends Number> filter) {
		// TODOLF impl HibernateSearchFilterQueryBuilder.add
		return null;
	}

	@Override
	public FullTextQuery build() {
		return context.getHibernateSearch().buildQuery(
			new ElasticsearchJsonQueryDescriptor(EQL_BUILDER.toJson(context.getEqlRoot())), context.getEntityClass());
	}

	// TODOLF copy & paste from HibernateSearchFilterQueryBuilder
	@SuppressWarnings("unchecked")
	protected <T> Page<T> execute(Pageable pageable, Sortable<?> sortable, Consumer<FullTextQuery> queryCustomizer,
								  Function<List<?>, List<T>> resultsTransformer) {
		if (pageable==null)
			pageable = BasePageableRequest.ofUnpaged();
		if (sortable==null)
			sortable = BasePageableRequest.ofUnpaged();

		FullTextQuery fullTextQuery = build();
		if (queryCustomizer!=null)
			queryCustomizer.accept(fullTextQuery);

		if (logger.isTraceEnabled())
			logger.trace("Executing lucene query: {}", fullTextQuery.toString());

		long count = fullTextQuery.getResultSize();

		if (pageable.isPaged()) {
			fullTextQuery.setFirstResult(pageable.getOffset());
			fullTextQuery.setMaxResults(pageable.getPageSize());
		}

		List<T> resultsList;
		if (resultsTransformer!=null)
			resultsList = resultsTransformer.apply(fullTextQuery.getResultList());
		else
			resultsList = (List<T>) fullTextQuery.getResultList();

		return buildPageableResult(pageable.getPageSize(), pageable.getPage(), count, resultsList);

	}

	@SuppressWarnings("unchecked")
	@Override
	public P list(Pageable pageable, Sortable<?> sortable) {
		return (P) execute(pageable, sortable, null, null);
	}

	protected EQLComponent createFieldQuery(FieldSearchStrategy strategy, String field, String query) {
		switch (strategy) {
			case DEFAULT:
				return EQLMatchComponent.of(field, EQLMatchQuery.of(query).withAutoFuzziness());
			case WILDCARD_PHRASE:
				return EQLMatchPhrasePrefixComponent.of(field, EQLMatchPhrasePrefixQuery.of(query));
			default:
				throw new IllegalStateException(String.format("Strategy: %s is not implemented", strategy));
		}
	}

	public static Map<String, FieldSearchStrategy> defaultSearchFields() {
		Map<String, FieldSearchStrategy> map = new LinkedHashMap<>();
		map.put(HibernateSearch.FIELD_TEXT, FieldSearchStrategy.DEFAULT);
		map.put(HibernateSearch.FIELD_ID, FieldSearchStrategy.WILDCARD_PHRASE);
		return map;
	}


}
