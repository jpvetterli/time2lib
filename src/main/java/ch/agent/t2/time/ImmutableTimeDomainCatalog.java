/*
 *   Copyright 2011-2017 Hauser Olsson GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package ch.agent.t2.time;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;

/**
 * ImmutableTimeDomainCatalog implements the behavior of an immutable time
 * domain catalog. It provides only an empty catalog and is in itself useless.
 * Subclasses pass time domains in their constructors.
 * 
 * @author Jean-Paul Vetterli
 */
public abstract class ImmutableTimeDomainCatalog implements TimeDomainCatalog {
	
	private final Map<String, TimeDomain> domains;

	/**
	 * Constructor. A useful catalog should have at least one domain.
	 * 
	 * @param domains zero or more time domains
	 */
	public ImmutableTimeDomainCatalog(TimeDomain... domains) {
		this.domains = new HashMap<String, TimeDomain>();
		int i = 0;
		for (TimeDomain domain : domains) {
			i++;
			try {
				String label = verify(i, domain);
				this.domains.put(label, domain);
			} catch (Exception e) {
				throw T2Msg.runtimeException(K.T0001, e);
			}
		}
	}
	
	private String verify(int i, TimeDomain domain) throws T2Exception {
		String label = domain.getLabel();
		if (label == null || label.length() == 0)
			throw T2Msg.exception(K.T0014, i);
		if (domains.containsKey(label))
			throw T2Msg.exception(K.T0015, i, label);
		for (TimeDomain d : domains.values()) {
			if (domain.similar(d))
				throw T2Msg.exception(K.T0016, label, d.getLabel());
		}
		return label;
	}
	
	@Override
	public Collection<TimeDomain> get() {
		Collection<TimeDomain> d = new ArrayList<TimeDomain>(domains.size());
		d.addAll(domains.values());
		return d;
	}

	@Override
	public TimeDomain get(String label) throws T2Exception {
		TimeDomain domain = domains.get(label);
		if (domain == null)
			throw T2Msg.exception(K.T0006, label, getTimeDomainLabels().toString());
		return domain;
	}
	
	@Override
	public TimeDomain get(TimeDomainDefinition def) {
		TimeDomain domain = null;
		for (TimeDomain d : domains.values()) {
			if (d.similar(def.asTimeDomain())) {
				domain = d;
				break;
			}
		}
		return domain;
	}
	
	/**
	 * Return all time domain labels as a sorted collection.
	 * @return sorted collection of time domain labels
	 */
	private Collection<String> getTimeDomainLabels() {
		SortedSet<String> labels = new TreeSet<String>();
		for (String label : domains.keySet())
			labels.add(label);
		return labels;
	}

	
}
