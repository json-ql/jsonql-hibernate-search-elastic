package com.lifeinide.jsonql.hibernate.search.elastic.test;

import com.lifeinide.jsonql.core.dto.Page;
import com.lifeinide.jsonql.core.test.JsonQLBaseQueryBuilderTest;
import com.lifeinide.jsonql.hibernate.search.elastic.DefaultHibernateSearchElasticFilterQueryBuilder;
import com.lifeinide.jsonql.hibernate.search.elastic.HibernateSearchElasticFilterQueryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
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
	HibernateSearchElasticFilterQueryBuilder<HibernateSearchElasticEntity, Page<HibernateSearchElasticEntity>>
> {

	public static final String PERSISTENCE_UNIT_NAME = "test-jpa";
	public static final String SEARCHABLE_STRING = "in the middle of nowhere";
	public static final String SEARCHABLE_STRING_PART = "middle";

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
	protected void doTest(BiConsumer<EntityManager, HibernateSearchElasticFilterQueryBuilder<HibernateSearchElasticEntity, Page<HibernateSearchElasticEntity>>> c) {
		doWithEntityManager(em -> c.accept(em,
			new HibernateSearchElasticFilterQueryBuilder<>(em, HibernateSearchElasticEntity.class, SEARCHABLE_STRING)));
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


}
