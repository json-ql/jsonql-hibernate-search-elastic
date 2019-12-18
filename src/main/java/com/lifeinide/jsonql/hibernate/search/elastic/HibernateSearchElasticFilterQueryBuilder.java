package com.lifeinide.jsonql.hibernate.search.elastic;

import com.google.gson.JsonObject;
import com.lifeinide.jsonql.core.dto.BasePageableRequest;
import com.lifeinide.jsonql.core.dto.Page;
import com.lifeinide.jsonql.core.enums.QueryConjunction;
import com.lifeinide.jsonql.core.filters.*;
import com.lifeinide.jsonql.core.intr.*;
import com.lifeinide.jsonql.elasticql.EQLBuilder;
import com.lifeinide.jsonql.elasticql.enums.EQLSortOrder;
import com.lifeinide.jsonql.elasticql.node.EQLHighlight;
import com.lifeinide.jsonql.elasticql.node.EQLSort;
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
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.TwoWayFieldBridge;
import org.hibernate.search.elasticsearch.impl.ElasticsearchJsonQueryDescriptor;
import org.hibernate.search.elasticsearch.indexes.ElasticsearchIndexFamily;
import org.hibernate.search.elasticsearch.indexes.ElasticsearchIndexFamilyType;
import org.hibernate.search.elasticsearch.query.impl.ElasticsearchHSQueryImpl;
import org.hibernate.search.exception.SearchException;
import org.hibernate.search.indexes.IndexFamily;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.query.dsl.sort.SortContext;
import org.hibernate.search.query.dsl.sort.SortNativeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;
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
 * Please see them <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-analyzers.html">here</a>
 * or see hints at the bottom of this comment.
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
 * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-keyword-analyzer.html">here</a> .
 * You can also see some hints at the bottom of this comment.
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
 * <h2>Highlighting support</h2>
 *
 * This query builder supports search results highlighting with ElasticSearch low level client. For more info please take a look at
 * {@link #highlight(Pageable, Sortable)} and other methods from hightlighting section.
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
 * <h2>Analyzer hints</h2>
 *
 * Here is the recommended set of analyzers to be used for English language analysis. For {@link HibernateSearch#FIELD_TEXT}:
 *
 * <pre>{@code
 * @AnalyzerDef(name = "myEnglishAnalyzer",
 * 	tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
 * 	filters = {
 *        @TokenFilterDef(factory = LowerCaseFilterFactory.class),
 *        @TokenFilterDef(name = "my_stopwords", factory = ElasticsearchTokenFilterFactory.class, params = {
 *               @Parameter(name = "type", value = "stop"),
 *               @Parameter(name = "stopwords", value "_english_")
 *        }),
 *        @TokenFilterDef(factory = SnowballPorterFilterFactory.class, params = {
 *            @Parameter(name = "language", value = "English")
 *        })
 *   },
 *   charFilters = {
 *        @CharFilterDef(factory = HTMLStripCharFilterFactory.class) // optional for HTML tags stripping
 *   }
 * )}</pre>
 *
 * And for {@link HibernateSearch#FIELD_ID}:
 *
 * <pre>{@code
 * @AnalyzerDef(name = MySQLSearchConstants.ID_ANALYZER,
 * 	tokenizer = @TokenizerDef(factory = KeywordTokenizerFactory.class),
 * 	filters = {
 *    @TokenFilterDef(factory = LowerCaseFilterFactory.class)
 *  }
 * )}</pre>
 *
 * @see HibernateSearchFilterQueryBuilder More information about Hibernate Search-related filtering.
 * @author Lukasz Frankowski
 */
public class HibernateSearchElasticFilterQueryBuilder<E, H extends ElasticSearchHighlightedResults<E>, P extends Page<E>, PH extends Page<H>>
extends BaseHibernateSearchFilterQueryBuilder<E, P, HibernateSearchElasticQueryBuilderContext<E>,
	HibernateSearchElasticFilterQueryBuilder<E, H, P, PH>> {

	public static final Logger logger = LoggerFactory.getLogger(HibernateSearchElasticFilterQueryBuilder.class);
	public static final EQLBuilder EQL_BUILDER = new EQLBuilder(false);
	public static final Class GLOBAL_SEARCH_CLASS = Object.class;

	protected HibernateSearchElasticQueryBuilderContext<E> context;
	protected Map<String, FieldSearchStrategy> searchableFields;
	protected boolean global = false; // indicates global search instead of concrete entity type search

	/**
	 * Builds a query builder for concrete entity class with default search fields.
	 */
	public HibernateSearchElasticFilterQueryBuilder(@Nonnull EntityManager entityManager, @Nonnull Class<E> entityClass, @Nullable String q) {
		this(entityManager, entityClass, q, defaultSearchFields());
	}

	/**
	 * Builds a query builder for concrete entity class with customizable search fields.
	 */
	public HibernateSearchElasticFilterQueryBuilder(@Nonnull EntityManager entityManager, @Nonnull Class<E> entityClass,
													@Nullable String q, @Nullable Map<String, FieldSearchStrategy> fields) {
		if (GLOBAL_SEARCH_CLASS.equals(entityClass))
			global = true;

		HibernateSearch hibernateSearch = new HibernateSearch(entityManager);
		context = new HibernateSearchElasticQueryBuilderContext<>(q, entityClass, hibernateSearch);

		boolean fieldFound = false;
		this.searchableFields = fields!=null ? fields : new HashMap<>();

		if (fields!=null && q!=null)
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
	public HibernateSearchElasticFilterQueryBuilder(@Nonnull EntityManager entityManager, @Nullable String q,
													@Nullable Map<String, FieldSearchStrategy> fields) {
		this(entityManager, GLOBAL_SEARCH_CLASS, q, fields);
	}

	/**
	 * Builds a global query builder with default search fields.
	 */
	public HibernateSearchElasticFilterQueryBuilder(@Nonnull EntityManager entityManager, @Nullable String q) {
		this(entityManager, q, defaultSearchFields());
	}

	@Nonnull
	@Override
	public HibernateSearchElasticQueryBuilderContext<E> context() {
		return context;
	}

	@Nonnull
	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, H, P, PH> add(@Nonnull String field, DateRangeQueryFilter filter) {
		if (filter!=null)
			addRangeQuery(field, filter.calculateFrom(), filter.calculateTo(), false);
		return this;
	}

	@Nonnull
	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, H, P, PH> add(@Nonnull String field, EntityQueryFilter<?> filter) {
		return add(field, (SingleValueQueryFilter<?>) filter);
	}

	@Nonnull
	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, H, P, PH> add(@Nonnull String field, ListQueryFilter<? extends QueryFilter> filter) {
		if (filter!=null && !filter.getFilters().isEmpty()) {
			HibernateSearchElasticFilterQueryBuilder<E, H, P, PH> internalBuilder =
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

	@Nonnull
	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, H, P, PH> add(@Nonnull String field, SingleValueQueryFilter<?> filter) {
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

	@Nonnull
	@Override
	public HibernateSearchElasticFilterQueryBuilder<E, H, P, PH> add(@Nonnull String field, ValueRangeQueryFilter<? extends Number> filter) {
		if (filter!=null)
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

	@Nonnull
	@Override
	public FullTextQuery build(@Nonnull Pageable pageable, @Nonnull Sortable<?> sortable) {
		return context.getHibernateSearch().buildQuery(
			new ElasticsearchJsonQueryDescriptor(EQL_BUILDER.toJson(context.getEqlRoot())), context.getEntityClass());
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	@Override
	public P list(Pageable pageable, Sortable<?> sortable) {
		return (P) execute(pageable, sortable, defaultSortCustomizer(sortable), null);
	}

	/**********************************************************************************************************
	 * Highlight support
	 **********************************************************************************************************/

	public static final int MAX_HIGHLIGHT_RESULT_WINDOW_SIZE = 10000;

	protected static Map<Class, SearchableEntityInfo> entityInfoCache = new HashMap<>();

	/**
	 * Builds appropriate hightlight result. To be overwritten in subclasses if necessary.
	 */
	@SuppressWarnings("unchecked")
	protected H buildHighlight(String id, String type, double score, String highlight) {
		return (H) new ElasticSearchHighlightedResults<E>(id, type, score, highlight);
	}

	/**
	 * Discovers the entity id to be loaded after search results are fetched, because ids are stored in elastic as "keyword" type
	 * (String), we need to convert them to appropriate type to fetch the entity from EntityManager.
	 */
	protected SearchableEntityInfo loadEntityInfo(Class<?> entityClass) {
		return entityInfoCache.computeIfAbsent(entityClass, it -> {
			EntityType entityType = context.getHibernateSearch().entityManager().getMetamodel().entity(entityClass);
			String idName = entityType.getId(entityType.getIdType().getJavaType()).getName();
			FieldBridge fieldBridge = context.getHibernateSearch().fullTextEntityManager().getSearchFactory()
				.getIndexedTypeDescriptor(entityClass).getIndexedField(idName).getFieldBridge();
			if (fieldBridge instanceof TwoWayFieldBridge) {
				FieldType fieldType = new FieldType();
				fieldType.setStored(true);
				return new SearchableEntityInfo(entityType, idName, id -> {
					Document document = new Document();
					document.add(new Field(idName, id, fieldType));
					return ((TwoWayFieldBridge) fieldBridge).get(idName, document);
				});
			} else {
				logger.warn("Cannot convert id for entity: {} and field bridge: {}. The entity won't be fetched from db.",
					entityClass.getSimpleName(), fieldBridge);
				return new SearchableEntityInfo(entityType, idName, null);
			}
		});
	}

	protected SearchableEntityInfo loadEntityInfo(String entityClassName) {
		try {
			return loadEntityInfo(Class.forName(entityClassName));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Provides highlighted results in the same way as {@link #list(Pageable, Sortable)} provides entity results.
	 * <p>
	 * Hibernate Search doesn't support highlighting for ElasticSearch query and {@link ElasticsearchHSQueryImpl} is poorly written,
	 * and it doesn't allow to create extended classes to implement this feature. This is why with this feature we need to go with low
	 * level client and query.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	@Nonnull public PH highlight(@Nullable Pageable pageable, @Nullable Sortable<?> sortable) {

		if (pageable==null)
			pageable = BasePageableRequest.ofUnpaged();
		if (sortable==null)
			sortable = BasePageableRequest.ofUnpaged();

		// add highlight
		context.getEqlRoot().withHighlight(EQLHighlight.of(searchableFields.keySet()));

		// add sorting manually, because in defaultSortCustomizer() we put it on FullTextQuery (because HS cuts it off from the original query)
		sortable.getSort()
			.forEach(sort -> context.getEqlRoot().withSort(sort.getSortField(), sort.isDesc() ? EQLSort.ofDesc() : EQLSort.ofAsc()));

		// add paging manually, because in execute() we put it on FullTextQuery (because HS cuts it off from the original query)
		if (pageable.isPaged())
			context.getEqlRoot().withPage(pageable.getOffset(), pageable.getPageSize());
		else
			context.getEqlRoot().withPage(0, MAX_HIGHLIGHT_RESULT_WINDOW_SIZE);

		// extract ES low-level client
		SearchFactory searchFactory = context.getHibernateSearch().fullTextEntityManager().getSearchFactory();
		IndexFamily indexFamily = searchFactory.getIndexFamily(ElasticsearchIndexFamilyType.get());
		ElasticsearchIndexFamily elasticsearchIndexFamily = indexFamily.unwrap(ElasticsearchIndexFamily.class);
		RestClient restClient = elasticsearchIndexFamily.getClient(RestClient.class);

		// get the highlighted results
		try {
			// make request (to concrete entity index or _all index)
			String indexName = global ? "_all" : context.getIndexedTypeDescriptor().getIndexDescriptors().iterator().next().getName();
			Response httpResponse = restClient.performRequest(
				"POST",
				String.format("/%s/_search", indexName),
				new HashMap<>(),
				new NStringEntity(EQL_BUILDER.toJsonString(context.getEqlRoot()), ContentType.APPLICATION_JSON)
			);
			JsonObject jsonResponse = EQL_BUILDER
				.getGson()
				.fromJson(new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent())), JsonObject.class);

			// transform json results into a list of highlighted results
			List<ElasticSearchHighlightedResults<E>> resultList = new ArrayList<>();
			Map<SearchableEntityInfo, Map<Object, ElasticSearchHighlightedResults>> idMap = new LinkedHashMap<>();
			JsonObject hits = jsonResponse.getAsJsonObject("hits");
			long total = hits.get("total").getAsLong();
			hits.getAsJsonArray("hits").forEach(it -> {
				JsonObject el = (JsonObject) it;
				List<String> highlightList = new ArrayList<>();
				el.getAsJsonObject("highlight").entrySet().forEach(entry ->
					entry.getValue().getAsJsonArray().forEach(it1 -> highlightList.add(it1.getAsString())));

				H result = buildHighlight(
					el.get("_id").getAsString(),
					el.get("_type").getAsString(),
					el.get("_score").isJsonNull() ? 0 : el.get("_score").getAsDouble(),
					String.join(" ", highlightList.toArray(new String[0]))
				);

				resultList.add(result);

				// separate fetched entities by type and get its real converted id
				SearchableEntityInfo entityInfo = loadEntityInfo(result.getType());
				if (entityInfo.idConverter!=null)
					idMap.computeIfAbsent(entityInfo, it1 -> new LinkedHashMap<>())
						.put(entityInfo.idConverter.apply(result.getId()), result);
			});

			// having idMap filled we can now fetch real entities from the db and set them for the results list
			idMap.forEach((entityInfo, localIdMap) -> context.getHibernateSearch().entityManager()
				.createQuery(String.format("select e from %s e where %s in :idList",
						entityInfo.entityType.getName(),
						entityInfo.idName),
					entityInfo.entityType.getJavaType())
				.setParameter("idList", localIdMap.keySet())
				.getResultList()
				.forEach(entity -> {
					Object entityId = context.getHibernateSearch().entityManager()
						.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
					if (entityId != null) {
						ElasticSearchHighlightedResults result = localIdMap.get(entityId);
						if (result != null)
							result.setEntity(entity);
					}
				}));

			return (PH) buildPageableResult(pageable.getPageSize(), pageable.getPage(), total, resultList);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error fetching results from ES low level client", e);
		}
	}

	/** @see #highlight(Pageable, Sortable)  **/
	@Nonnull public PH highlight() {
		return highlight(null, null);
	}

	/** @see #highlight(Pageable, Sortable)  **/
	@Nonnull public PH highlight(@Nullable Pageable pageable) {
		return highlight(pageable, null);
	}

	/** @see #highlight(Pageable, Sortable)  **/
	@Nonnull public PH highlight(@Nullable Sortable<?> sortable) {
		return highlight(null, sortable);
	}

	/** @see #highlight(Pageable, Sortable)  **/
	@Nonnull public PH highlight(@Nullable PageableSortable<?> ps) {
		return highlight(ps, ps);
	}

	protected static class SearchableEntityInfo {
		@Nonnull protected EntityType entityType;
		@Nonnull protected String idName;
		@Nullable protected Function<String, Object> idConverter;

		public SearchableEntityInfo(@Nonnull EntityType entityType, @Nonnull String idName, @Nullable Function<String, Object> idConverter) {
			this.entityType = entityType;
			this.idName = idName;
			this.idConverter = idConverter;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof SearchableEntityInfo)) return false;
			SearchableEntityInfo that = (SearchableEntityInfo) o;
			return entityType.getJavaType().equals(that.entityType.getJavaType());
		}

		@Override
		public int hashCode() {
			return Objects.hash(entityType.getJavaType());
		}

		@Override
		public String toString() {
			return "SearchableEntityInfo{" +
				"entityType=" + entityType.getJavaType() +
				'}';
		}
	}

	/**********************************************************************************************************
	 * Other stuff
	 **********************************************************************************************************/

	protected Consumer<FullTextQuery> defaultSortCustomizer(Sortable<?> sortable) {
		return fte -> {
			if (sortable!=null && !sortable.getSort().isEmpty()) {
				SortContext sortContext = context.getQueryBuilder().sort();
				SortNativeContext sortNativeContext = null;
				for (SortField sort: sortable.getSort()) {
					String by = String.format("{\"order\": \"%s\"}", sort.isAsc() ? EQLSortOrder.asc : EQLSortOrder.desc);
					if (sortNativeContext!=null)
						sortNativeContext = sortNativeContext.andByNative(sort.getSortField(), by);
					else
						sortNativeContext = sortContext.byNative(sort.getSortField(), by);
				}
				if (sortNativeContext!=null)
					fte.setSort(sortNativeContext.andByScore().createSort());
			}
		};
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
