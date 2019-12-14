package com.lifeinide.jsonql.hibernate.search.elastic;

import com.lifeinide.jsonql.core.dto.Page;
import com.lifeinide.jsonql.core.enums.QueryConjunction;
import com.lifeinide.jsonql.core.filters.*;
import com.lifeinide.jsonql.core.intr.FilterQueryBuilder;
import com.lifeinide.jsonql.core.intr.QueryFilter;
import com.lifeinide.jsonql.elasticql.EQLBuilder;
import com.lifeinide.jsonql.elasticql.node.component.*;
import com.lifeinide.jsonql.elasticql.node.query.*;
import com.lifeinide.jsonql.hibernate.search.BaseHibernateSearchFilterQueryBuilder;
import com.lifeinide.jsonql.hibernate.search.FieldSearchStrategy;
import com.lifeinide.jsonql.hibernate.search.HibernateSearch;
import com.lifeinide.jsonql.hibernate.search.HibernateSearchFilterQueryBuilder;
import com.lifeinide.jsonql.hibernate.search.bridge.BaseDomainFieldBridge;
import com.lifeinide.jsonql.hibernate.search.bridge.BigDecimalRangeBridge;
import com.lifeinide.jsonql.hibernate.search.elastic.bridge.BaseElasticDomainFieldBridge;
import com.lifeinide.jsonql.hibernate.search.elastic.bridge.ElasticBigDecimalRangeBridge;
import org.hibernate.search.elasticsearch.impl.ElasticsearchJsonQueryDescriptor;
import org.hibernate.search.exception.SearchException;
import org.hibernate.search.jpa.FullTextQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

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
 * <h3>Field bridge for {@link BigDecimal}</h3>
 *
 * Use {@link ElasticBigDecimalRangeBridge} in the same way as {@link BigDecimalRangeBridge} is used in
 * {@link HibernateSearchFilterQueryBuilder} example.
 *
 * <h3>Field bridge for entities</h3>
 *
 * Use {@link BaseElasticDomainFieldBridge} in the same way as {@link BaseDomainFieldBridge} is used in
 * {@link HibernateSearchFilterQueryBuilder} example.
 *
 * <h2>Example json with full text search and filters</h2>
 *
 * <pre>{@code
 * {
 *   "query": {
 *     "bool": {
 *       "should": [
 *         {
 *           "match": {
 *             "text": {
 *               "fuzziness": "AUTO",
 *               "query": "in the middle of nowhere"
 *             }
 *           }
 *         },
 *         {
 *           "match_phrase_prefix": {
 *             "textid": {
 *               "query": "in the middle of nowhere"
 *             }
 *           }
 *         }
 *       ],
 *       "filter": [
 *         {
 *           "bool": {
 *             "must": [
 *               {
 *                 "exists": {
 *                   "field": "stringVal"
 *                 }
 *               }
 *             ]
 *           }
 *         }
 *       ]
 *     }
 *   }
 * }}</pre>
 *
 * @see HibernateSearchFilterQueryBuilder More information about Hibernate Search-related filtering.
 * @author Lukasz Frankowski
 */
public class HibernateSearchElasticFilterQueryBuilder<E, P extends Page<E>>
extends BaseHibernateSearchFilterQueryBuilder<E, P, HibernateSearchElasticQueryBuilderContext<E>, HibernateSearchElasticFilterQueryBuilder<E, P>> {

	public static final Logger logger = LoggerFactory.getLogger(HibernateSearchElasticFilterQueryBuilder.class);
	protected static final EQLBuilder EQL_BUILDER = new EQLBuilder();

	protected HibernateSearchElasticQueryBuilderContext<E> context;

	/**
	 * Builds a query builder for concrete entity class with default search fields.
	 */
	public HibernateSearchElasticFilterQueryBuilder(EntityManager entityManager, Class<E> entityClass, String q) {
		this(entityManager, entityClass, q, defaultSearchFields());
	}

	/**
	 * Builds a query builder for concrete entity class with customizable search fields.
	 */
	public HibernateSearchElasticFilterQueryBuilder(EntityManager entityManager, Class<E> entityClass, String q,
											 		Map<String, FieldSearchStrategy> fields) {
		HibernateSearch hibernateSearch = new HibernateSearch(entityManager);
		context = new HibernateSearchElasticQueryBuilderContext<>(q, entityClass, hibernateSearch);

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

	/**
	 * Builds a global query builder with customizable search fields.
	 */
	@SuppressWarnings("unchecked")
	public HibernateSearchElasticFilterQueryBuilder(EntityManager entityManager, String q, Map<String, FieldSearchStrategy> fields) {
		this(entityManager, (Class) Object.class, q, fields);
	}

	/**
	 * Builds a global query builder with default search fields.
	 */
	public HibernateSearchElasticFilterQueryBuilder(EntityManager entityManager, String q) {
		this(entityManager, q, defaultSearchFields());
	}

	@Override
	public HibernateSearchElasticQueryBuilderContext<E> context() {
		return context;
	}

	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, P> add(String field, DateRangeQueryFilter filter) {
		addRangeQuery(field, filter.calculateFrom(), filter.calculateTo(), false);
		return this;
	}

	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, P> add(String field, EntityQueryFilter<?> filter) {
		return add(field, (SingleValueQueryFilter<?>) filter);
	}

	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, P> add(String field, ListQueryFilter<? extends QueryFilter> filter) {
		if (filter!=null && !filter.getFilters().isEmpty()) {
			HibernateSearchElasticFilterQueryBuilder<E, P> internalBuilder =
				new HibernateSearchElasticFilterQueryBuilder<>(
					context.getHibernateSearch().entityManager(), context.getEntityClass(), context.getQuery());

			filter.getFilters().forEach(f -> f.accept(internalBuilder, field));

			switch (filter.getConjunction()) {

				// for "and" query we can just take the same bool as produced from the internal builder and use it in this builder filters
				case and:
					context.getEqlFilterBool().withMust(EQLBoolComponent.of(internalBuilder.context().getEqlFilterBool()));
					break;

				// for "or" query we can need to convert "must" to "should", and "must_not"
				case or:
					internalBuilder.context().getEqlFilterBool().getShould().forEach(component ->
						context.getEqlFilterBool().withShould(component));
					internalBuilder.context().getEqlFilterBool().getMust().forEach(component ->
						context.getEqlFilterBool().withShould(component));
					internalBuilder.context().getEqlFilterBool().getMustNot().forEach(component ->
						context.getEqlFilterBool().withShould(EQLBoolComponent.of(EQLBool.of().withMustNot(component))));
					break;

				default:
					throw new IllegalStateException("Unexpected value: " + QueryConjunction.and.equals(filter.getConjunction()));
			}

		}
		
		return this;
	}

	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, P> add(String field, SingleValueQueryFilter<?> filter) {
		if (filter!=null)

			switch (filter.getCondition()) {

				case eq:
					context.getEqlFilterBool().withMust(EQLTermComponent.of(field, EQLTermQuery.of(filter.getValue())));
					break;

				case ne:
					context.getEqlFilterBool().withMustNot(EQLTermComponent.of(field, EQLTermQuery.of(filter.getValue())));
					break;

				case gt:
					context.getEqlFilterBool().withMust(EQLRangeComponent.of(field, EQLRangeQuery.ofGt(filter.getValue())));
					break;

				case ge:
					context.getEqlFilterBool().withMust(EQLRangeComponent.of(field, EQLRangeQuery.ofGte(filter.getValue())));
					break;

				case lt:
					context.getEqlFilterBool().withMust(EQLRangeComponent.of(field, EQLRangeQuery.ofLt(filter.getValue())));
					break;

				case le:
					context.getEqlFilterBool().withMust(EQLRangeComponent.of(field, EQLRangeQuery.ofLte(filter.getValue())));
					break;

				case isNull:
					context.getEqlFilterBool().withMustNot(EQLExistsComponent.of(EQLExistsQuery.of(field)));
					break;

				case notNull:
					context.getEqlFilterBool().withMust(EQLExistsComponent.of(EQLExistsQuery.of(field)));
					break;

				default:
					throw new IllegalArgumentException(
						String.format("Condition: %s not supported for HibernateSearchFilterQueryBuilder", filter.getCondition()));

			}

		return this;
	}

	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, P> add(String field, ValueRangeQueryFilter<? extends Number> filter) {
		addRangeQuery(field, filter.getFrom(), filter.getTo(), true);
		return this;
	}

	protected <T> EQLRangeQuery<T> addRangeQuery(String field, T from, T to, boolean lte) {
		EQLRangeQuery<T> query = new EQLRangeQuery<>();
		if (from!=null)
			query.setGte(from);
		if (to!=null) {
			if (lte)
				query.setLte(to);
			else
				query.setLt(to);
		}

		context.getEqlFilterBool().getMust().add(EQLRangeComponent.of(field, query));
		return query;
	}

	@Override
	public FullTextQuery build() {
		return context.getHibernateSearch().buildQuery(
			new ElasticsearchJsonQueryDescriptor(EQL_BUILDER.toJson(context.getEqlRoot())), context.getEntityClass());
	}

	// TODOLF add highlight support

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

	@Override
	protected Logger logger() {
		return logger;
	}

	public static Map<String, FieldSearchStrategy> defaultSearchFields() {
		Map<String, FieldSearchStrategy> map = new LinkedHashMap<>();
		map.put(HibernateSearch.FIELD_TEXT, FieldSearchStrategy.DEFAULT);
		map.put(HibernateSearch.FIELD_ID, FieldSearchStrategy.WILDCARD_PHRASE);
		return map;
	}


}
