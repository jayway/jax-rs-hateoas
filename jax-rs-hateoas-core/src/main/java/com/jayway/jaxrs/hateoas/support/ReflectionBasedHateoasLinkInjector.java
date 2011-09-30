/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jayway.jaxrs.hateoas.support;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.jayway.jaxrs.hateoas.HateoasInjectException;
import com.jayway.jaxrs.hateoas.HateoasLink;
import com.jayway.jaxrs.hateoas.HateoasLinkInjector;
import com.jayway.jaxrs.hateoas.HateoasVerbosity;

/**
 * @author Mattias Hellborg Arthursson
 * @author Kalle Stenflo
 */
public class ReflectionBasedHateoasLinkInjector implements HateoasLinkInjector {

	private static final String DEFAULT_LINKS_FIELD_NAME = "links";
	private final String linksFieldName;

	public ReflectionBasedHateoasLinkInjector() {
		this(DEFAULT_LINKS_FIELD_NAME);
	}

	public ReflectionBasedHateoasLinkInjector(String linksFieldName) {
		this.linksFieldName = linksFieldName;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public Object injectLinks(Object entity, Collection<HateoasLink> links,
			final HateoasVerbosity verbosity) {
		try {
			if (Collection.class.isAssignableFrom(entity.getClass())) {
				Collection<Object> entityAsCollection = (Collection<Object>) entity;
				entity = new HateoasCollectionWrapper(entityAsCollection);
			}

			Field field = ReflectionUtils.getField(entity, linksFieldName);

			if (Collection.class.isAssignableFrom(field.getType())) {
				field.set(entity, Collections2.transform(links,
						new Function<HateoasLink, Map<String, Object>>() {
							@Override
							public Map<String, Object> apply(HateoasLink from) {
								return from.toMap(verbosity);
							}
						}));
			} else {
				throw new IllegalArgumentException("Field '" + linksFieldName
						+ "' is not a Collection object");
			}

			return entity;
		} catch (Exception e) {
			throw new HateoasInjectException(e);
		}
	}
}
