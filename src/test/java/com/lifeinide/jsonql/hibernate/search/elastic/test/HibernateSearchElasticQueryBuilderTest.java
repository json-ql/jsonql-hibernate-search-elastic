package com.lifeinide.jsonql.hibernate.search.elastic.test;

import com.lifeinide.jsonql.core.dto.BasePageableRequest;
import com.lifeinide.jsonql.core.dto.Page;
import com.lifeinide.jsonql.core.intr.Pageable;
import com.lifeinide.jsonql.core.intr.Sortable;
import com.lifeinide.jsonql.core.test.IJsonQLBaseTestEntity;
import com.lifeinide.jsonql.core.test.JsonQLBaseQueryBuilderTest;
import com.lifeinide.jsonql.hibernate.search.FieldSearchStrategy;
import com.lifeinide.jsonql.hibernate.search.elastic.DefaultHibernateSearchElasticFilterQueryBuilder;
import com.lifeinide.jsonql.hibernate.search.elastic.ElasticSearchHighlightedResults;
import com.lifeinide.jsonql.hibernate.search.elastic.HibernateSearchElasticFilterQueryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Lukasz Frankowski
 */
public class HibernateSearchElasticQueryBuilderTest extends JsonQLBaseQueryBuilderTest<
	EntityManager,
	Long,
	HibernateSearchElasticAssociatedEntity,
	HibernateSearchElasticEntity,
	HibernateSearchElasticFilterQueryBuilder<
		HibernateSearchElasticEntity,
		ElasticSearchHighlightedResults<HibernateSearchElasticEntity>,
		Page<HibernateSearchElasticEntity>,
		Page<ElasticSearchHighlightedResults<HibernateSearchElasticEntity>>
	>
> {

	public static final String PERSISTENCE_UNIT_NAME = "test-jpa";
	public static final String SEARCHABLE_STRING = "in the middle of nowhere";
	public static final String SEARCHABLE_STRING_PART = "middle";
	public static final String HIGHLIGHTED_SEARCHABLE_STRING = "<em>in</em> <em>the</em> <em>middle</em> <em>of</em> <em>nowhere</em>";

	protected EntityManagerFactory entityManagerFactory;

	@BeforeAll
	public void init() {
		entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		doWithEntityManager(em -> populateData(em::persist));
	}

	@AfterAll
	public void done() {
		if (entityManagerFactory!=null)
			entityManagerFactory.close();
	}

	@Override
	protected HibernateSearchElasticEntity buildEntity(Long previousId) {
		return new HibernateSearchElasticEntity(previousId==null ? 1L : previousId+1);
	}

	@Override
	protected HibernateSearchElasticAssociatedEntity buildAssociatedEntity() {
		return new HibernateSearchElasticAssociatedEntity(1L);
	}

	@Override
	protected void doTest(BiConsumer<EntityManager, HibernateSearchElasticFilterQueryBuilder<
		HibernateSearchElasticEntity,
		ElasticSearchHighlightedResults<HibernateSearchElasticEntity>,
		Page<HibernateSearchElasticEntity>,
		Page<ElasticSearchHighlightedResults<HibernateSearchElasticEntity>>>> c) {

		// test for standard list()
		doWithEntityManager(em -> c.accept(em,
			new HibernateSearchElasticFilterQueryBuilder<>(em, HibernateSearchElasticEntity.class, SEARCHABLE_STRING)));

		// test for highlight()
		doWithEntityManager(em -> c.accept(em,
			new HighlightingHibernateSearchElasticFilterQueryBuilder<HibernateSearchElasticEntity>(em, HibernateSearchElasticEntity.class, SEARCHABLE_STRING)));
	}

	@Test
	public void testLocalAndGlobalSearch() {
		doWithEntityManager(em -> {
			DefaultHibernateSearchElasticFilterQueryBuilder<?> qb =
				new DefaultHibernateSearchElasticFilterQueryBuilder<>(em, HibernateSearchElasticEntity.class, SEARCHABLE_STRING_PART);
			Page<?> page = qb.list();
			Assertions.assertEquals(100, page.getCount());

			qb = new DefaultHibernateSearchElasticFilterQueryBuilder<>(em, SEARCHABLE_STRING_PART);
			page = qb.list();
			Assertions.assertEquals(101, page.getCount());
		});
	}

	@SuppressWarnings("ConstantConditions")
	@Test
	public void testHighlight() {
		// concrete entity search
		doTest((em, qb) -> {
			Page<ElasticSearchHighlightedResults<HibernateSearchElasticEntity>> results = qb.highlight(BasePageableRequest.ofDefault().withPageSize(20));
			Assertions.assertEquals(100, results.getCount());
			Assertions.assertEquals(20, results.getData().size());
			results.getData().forEach(it -> {
				Assertions.assertEquals(HIGHLIGHTED_SEARCHABLE_STRING, it.getHighlight());
				Assertions.assertEquals(HibernateSearchElasticEntity.class.getName(), it.getType());
				Assertions.assertEquals(it.getEntity().getId().toString(), it.getId());
			});
		});

		// global search
		doWithEntityManager(em -> {
			DefaultHibernateSearchElasticFilterQueryBuilder<IJsonQLBaseTestEntity> qb =
				new DefaultHibernateSearchElasticFilterQueryBuilder<>(em, SEARCHABLE_STRING);
			Page<ElasticSearchHighlightedResults<IJsonQLBaseTestEntity>> results =
				qb.highlight(BasePageableRequest.ofDefault().withPageSize(20));
			Assertions.assertEquals(101, results.getCount());
			Assertions.assertEquals(20, results.getData().size());
			results.getData().forEach(it -> {
				Assertions.assertEquals(HIGHLIGHTED_SEARCHABLE_STRING, it.getHighlight());
				try {
					Assertions.assertEquals(HibernateSearchElasticEntity.class.getName(), it.getType());
				} catch (AssertionFailedError e) {
					Assertions.assertEquals(HibernateSearchElasticAssociatedEntity.class.getName(), it.getType());
				}
				Assertions.assertEquals(it.getEntity().getId().toString(), it.getId());
			});
		});
	}

	protected void doWithEntityManager(Consumer<EntityManager> c) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		try {
			c.accept(entityManager);
		} finally {
			entityManager.getTransaction().commit();
			entityManager.close();
		}
	}

	/**
	 * This query builder only forwards highlighted results as a standard results for tests.
	 */
	protected class HighlightingHibernateSearchElasticFilterQueryBuilder<E>
		extends HibernateSearchElasticFilterQueryBuilder<E, ElasticSearchHighlightedResults<E>, Page<E>,
		Page<ElasticSearchHighlightedResults<E>>> {

		public HighlightingHibernateSearchElasticFilterQueryBuilder(@Nonnull EntityManager entityManager, @Nonnull Class<E> entityClass, @Nullable String q) {
			super(entityManager, entityClass, q);
		}

		public HighlightingHibernateSearchElasticFilterQueryBuilder(@Nonnull EntityManager entityManager, @Nonnull Class<E> entityClass, @Nullable String q, @Nullable Map<String, FieldSearchStrategy> fields) {
			super(entityManager, entityClass, q, fields);
		}

		public HighlightingHibernateSearchElasticFilterQueryBuilder(@Nonnull EntityManager entityManager, @Nullable String q, @Nullable Map<String, FieldSearchStrategy> fields) {
			super(entityManager, q, fields);
		}

		public HighlightingHibernateSearchElasticFilterQueryBuilder(@Nonnull EntityManager entityManager, @Nullable String q) {
			super(entityManager, q);
		}

		@Nonnull
		@Override
		public Page<E> list(Pageable pageable, Sortable<?> sortable) {
			return highlight(pageable, sortable).transform(ElasticSearchHighlightedResults::getEntity);
		}
		
	}


}
