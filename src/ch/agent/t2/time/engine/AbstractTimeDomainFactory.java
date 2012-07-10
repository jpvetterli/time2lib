/*
 *   Copyright 2011 Hauser Olsson GmbH
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
 * Package: ch.agent.t2.time.engine
 * Type: AbstractTimeDomainFactory
 * Version: 1.0.0
 */
package ch.agent.t2.time.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.agent.core.KeyedException;
import ch.agent.t2.T2Msg;
import ch.agent.t2.time.DefaultExternalFormat;
import ch.agent.t2.time.ExternalTimeFormat;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeDomainDefinition;
import ch.agent.t2.time.TimeDomainFactory;
import ch.agent.t2.time.TimeDomainManager;

/**
 * AbstractTimeDomainFactory implements behavior for concrete time domain
 * factories. A concrete class should be implemented as a singleton and
 * should provide a choice of built-in time domains.
 * <p>
 * As an illustration, the code for a custom factory, with three built-in domains <q>yearly</q>, 
 * <q>monthly</q> and <q>foo</q> is given below. The first two domains are available from
 * the <em>Time2</em> library and are self-registering. The <q>foo</q> domain is defined and 
 * registered by the factory {@link TimeDomainFactory#get(TimeDomainDefinition, boolean)} method.
 * <blockquote>
 * <pre>
public class CustomTimeDomainFactory extends AbstractTimeDomainFactory {
    private static class Singleton {
        private static CustomTimeDomainFactory factory;
        static {
            factory = new CustomTimeDomainFactory();
            factory.declareBuiltIn(Year.DEF.getLabel());
            factory.declareBuiltIn(Month.DEF.getLabel());
            TimeDomainDefinition bar = new TimeDomainDefinition("foo", Resolution.MIN, 42L);
            factory.get(new TimeDomainDefinition("foo", Resolution.MIN, 42L), true);
            factory.declareBuiltIn("foo");
            factory.lockBuiltIns();
       };
    }
    public static CustomTimeDomainFactory getInstance() {
        return Singleton.factory;
    }
    private CustomTimeDomainFactory() {
    }
}
 * </pre>
 * </blockquote>
 * 
 * The recommended way for applications to access a concrete factory 
 * is via the {@link TimeDomainManager}.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public abstract class AbstractTimeDomainFactory implements TimeDomainFactory {

	private Map<String, TimeFactory> domains;
	private Set<String> declaredBuiltIns;
	private TimeDomain[] builtIns;
	private ExternalTimeFormat timeFormat;

	/**
	 * The constructor invoked by subclasses does some initialization. 
	 */
	protected AbstractTimeDomainFactory() {
		domains = new HashMap<String, TimeFactory>();
		declaredBuiltIns = new LinkedHashSet<String>(); // linked to maintain add sequence
	}
	
	@Override
	public ExternalTimeFormat getExternalTimeFormat() {
		if (timeFormat == null)
			timeFormat = DefaultExternalFormat.getInstance();
		return timeFormat;
	}

	/**
	 * Set the external time format. When not set, the instance of {@link DefaultExternalFormat}
	 * will be used.
	 * 
	 * @param timeFormat
	 */
	protected void setExternalTimeFormat(ExternalTimeFormat timeFormat) {
		this.timeFormat = timeFormat;
	}
	
	@Override
	public Collection<TimeDomain> getTimeDomains() {
		Collection<TimeDomain> d = new ArrayList<TimeDomain>(domains.size());
		d.addAll(domains.values());
		return d;
	}

	@Override
	public TimeDomain get(String label) throws KeyedException {
		TimeDomain domain = domains.get(label);
		if (domain == null)
			throw T2Msg.exception(32222, label, getTimeDomainLabels().toString());
		return domain;
	}
	
	@Override
	public TimeDomain get(TimeDomainDefinition def, boolean register) {
		TimeDomain domain = get(def);
		if (domain == null) {
			if (register)
				domain = define(def);
		} else {
			if (def.getLabel() != null && !domain.getLabel().equals(def.getLabel()))
				throw new RuntimeException(new T2Msg(32004, domain.toString(), def.getLabel()).toString());
		}
		return domain;
	}

	@Override
	public TimeDomain get(TimeDomainDefinition def) {
		TimeDomain domain = null;
		for (TimeDomain d : domains.values()) {
			if (((TimeFactory) d).matches(def.getBaseUnit(), def.getOrigin(),	
					def.getBasePeriodPattern(), def.getSubPeriodPattern())) {
				domain = d;
				break;
			}
		}
		return domain;
	}
	
	/**
	 * Get the time domain corresponding to the given definition.
	 * The label is ignored.
	 * 
	 * @param def a non-null time domain definition
	 * @return a time domain corresponding to the definition, with the label ignored, or null
	 */
	private TimeDomain getTimeDomain(TimeDomainDefinition def) {
		TimeDomain domain = null;
		for (TimeDomain d : domains.values()) {
			if (((TimeFactory) d).matches(def.getBaseUnit(), def.getOrigin(), 
					def.getBasePeriodPattern(), def.getSubPeriodPattern())) {
				domain = d;
				break;
			}
		}
		return domain;
	}

	/**
	 * Return a time domain with the given properties. When the domain already exists, 
	 * the label is ignored. If the domain does not exist, it is created and 
	 * registered under the label provided, if not null.
	 * When the label is null, a unique label is generated. If a label
	 * is specified but it is already used by another domain, an 
	 * <b>unchecked</b> exception is thrown. 
	 * <p>
	 * When the domain needs to be created and the definition includes a sub period pattern, 
	 * there is a requirement that base period of the sub period pattern must be equal to the
	 * domain's base unit.
	 * <p>
	 * The method is synchronized to enforce domain uniqueness in this factory.
	 *  
	 * @param def a non-null time domain definition
	 * @return a time domain
	 */
	private synchronized TimeDomain define(TimeDomainDefinition def) {
		if (def.getBaseUnit() == null)
			throw new IllegalArgumentException("basePeriod");
		if (def.getSubPeriodPattern() != null && !def.getBaseUnit().equals(def.getSubPeriodPattern().getBasePeriod()))
			throw new IllegalArgumentException("basePeriods differ");
		// step 1: find domain matching required attributes
		TimeDomain domain = getTimeDomain(def);
		
		// step 2: if not found, verify that label is available, then create new domain
		if (domain == null) {
			if (def.getLabel() == null) {
				def.setLabel(inventLabel());
			} else {
				domain = domains.get(def.getLabel());
				if (domain != null)
					throw new RuntimeException(new T2Msg(32002, domain.toString()).toString());
			}
			ExternalTimeFormat externalFormat = getExternalTimeFormat();
			if (externalFormat == null)
				externalFormat = DefaultExternalFormat.getInstance();
			domain = new TimeFactory(def, externalFormat);
			domains.put(def.getLabel(), (TimeFactory)domain);
		}

		return domain;
	}
	
    /**
     * Generate a unique label.
     * 
     * @return a unique label
     */
    private String inventLabel() {
		String label = "domain" + (domains.size() + 1);
		int i = 0;
		String invented = label;
		while(true) {
			if (domains.get(invented) == null)
				break;
			i++;
			invented = label + "." + i;
		}
		return invented;
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

	@Override
	public void declareBuiltIn(String label) {
		if (builtIns != null)
			throw new IllegalStateException("built-ins are locked");
		if (label == null)
			throw new IllegalArgumentException("label null");
		declaredBuiltIns.add(label);
	}

	@Override
	public boolean isBuiltIn(TimeDomain domain) {
		resolveBuiltIns();
		return domain instanceof TimeFactory ? ((TimeFactory) domain).isBuiltIn() : false;
	}

	@Override
	public Collection<TimeDomain> getBuiltIns() {
		resolveBuiltIns();
		return Arrays.asList(builtIns);
	}
	
	@Override
	public void lockBuiltIns() {
		/*
		 * Note. It is too early to resolve built-ins now because we are 
		 * typically running in static initializer and it is possible 
		 * that some domain is not yet completely registered. The 
		 * usual consequence is "ExceptionInInitializerError".
		 */
		if (declaredBuiltIns == null)
			return;
		else
			builtIns = new TimeDomain[declaredBuiltIns.size()];
	}

	/**
	 * Process the set of built-in declarations.
	 * Modify management information in the built-in domains and 
	 * build an array of built-in domains. 
	 */
	private void resolveBuiltIns() {
	    lockBuiltIns();
		if (declaredBuiltIns == null)
			return;
		if (builtIns.length > 0) {
			int i = 0;
			for (String def : declaredBuiltIns) {
				try {
					TimeDomain d = get(def);
					((TimeFactory) d).markBuiltIn(i);
					builtIns[i] = d;
					i++;
				} catch (KeyedException e) {
					throw new RuntimeException(new T2Msg(32003, this.getClass().getCanonicalName()).toString(), e);
				}
			}
		}
		declaredBuiltIns = null;
	}
	
}
