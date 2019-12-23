package com.lifeinide.jsonql.hibernate.search.elastic;

import com.lifeinide.jsonql.elasticql.node.EQLRoot;
import com.lifeinide.jsonql.elasticql.node.component.EQLBoolComponent;
import com.lifeinide.jsonql.elasticql.node.query.EQLBool;
import com.lifeinide.jsonql.hibernate.search.BaseHibernateSearchQueryBuilderContext;
import com.lifeinide.jsonql.hibernate.search.HibernateSearch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Stack;

/**
 * @author Lukasz Frankowski
 */
public class HibernateSearchElasticQueryBuilderContext<E> extends BaseHibernateSearchQueryBuilderContext<E> {

	/** Query root **/
	protected EQLRoot eqlRoot;

	/** Main query bool. We always use query with {@code {query: {bool: {...}}}}. **/
	protected EQLBool eqlBool;

	/** Filter query bool. We always use query with {@code {filter: [{bool: {...}}]}}. **/
	protected Stack<EQLBool> eqlFilterBool = new Stack<>();

	public HibernateSearchElasticQueryBuilderContext(@Nullable String query, @Nonnull Class<E> entityClass,
													 @Nonnull HibernateSearch hibernateSearch) {
		super(query, entityClass, hibernateSearch);

		this.eqlBool = EQLBool.of();
		this.eqlRoot = EQLRoot.of().withQuery(EQLBoolComponent.of(this.eqlBool));
	}

	@Nonnull public EQLRoot getEqlRoot() {
		return eqlRoot;
	}

	@Nonnull public EQLBool getEqlBool() {
		return eqlBool;
	}

	@Nonnull public EQLBool getEqlFilterBool() {
		if (eqlFilterBool.isEmpty()) {
			eqlFilterBool.push(EQLBool.of());
			eqlBool.withFilter(EQLBoolComponent.of(eqlFilterBool.peek()));
		}

		return eqlFilterBool.peek();
	}

	public EQLBool doWithNewFilterBool(Runnable r) {
		this.eqlFilterBool.push(EQLBool.of());
		r.run();
		return this.eqlFilterBool.pop();
	}

}
