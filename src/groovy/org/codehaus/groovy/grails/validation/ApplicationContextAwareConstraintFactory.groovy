package org.codehaus.groovy.grails.validation

import org.codehaus.groovy.grails.exceptions.GrailsDomainException;
import org.codehaus.groovy.grails.validation.Constraint;
import org.codehaus.groovy.grails.validation.ConstraintFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Constraint Factory that allows dependencies to be injected to Constraints at runtime.
 * @author daniel
 *
 */
class ApplicationContextAwareConstraintFactory implements ConstraintFactory {
	
	private Class constraintClass;
	private ApplicationContext applicationContext;
	private List<String> beansToInject = [];

	/**
	 * Constructor requires the Application Context containing the beans you would like
	 * to inject, the Class of the Constraint to create, and a list of strings containing
	 * the names of the beans you would like to inject.
	 * <p>You will receive an Illegal Argument Exception if either applicationContext
	 * or constraint is null.
	 * @param applicationContext
	 * @param constraint
	 * @param beansToInject
	 */
	ApplicationContextAwareConstraintFactory(ApplicationContext applicationContext, 
			Class constraint, List<String> beansToInject) {
	    if(applicationContext == null) throw new IllegalArgumentException("Argument [applicationContext] cannot be null");
	    if(constraint == null || !Constraint.class.isAssignableFrom(constraint))
	        throw new IllegalArgumentException("Argument [constraint] must be an instance of " + Constraint.class);

	    this.applicationContext = applicationContext;
	    this.constraintClass = constraint;
	    
	    for(String beanName : beansToInject){
			boolean springContained = applicationContext.containsBean(beanName)
			boolean classContained = constraint.metaClass.hasMetaProperty(beanName) 
			
	    	if(springContained && classContained) {
				this.beansToInject.add(beanName)
	        }else{
	        	String message = springContained ? "" : "Bean ${beanName} does not exist in Application Context - Constraint: "
	        	message = message + (classContained ? "" : "  Property ${beanName} does not exist for Constraint: ")
	        	
	        	throw new IllegalArgumentException(message + Constraint.class)
			}
		}
	}
	
	private void injectDependencies( applicationContext,  constraint, beansToInject){
		for(String beanName : beansToInject){
			constraint[(beanName)] = applicationContext.getBean(beanName);
		}
	}

	/**
	 * Create an instance of the Constraint Class for this factory.  Injects dependencies specified by the Factory Constructor
	 */
	@Override
	public Constraint newInstance() {
	    try {
	     	Constraint instance = (Constraint) constraintClass.newInstance();
	        
	     	if(beansToInject){
		    	injectDependencies(applicationContext, instance, beansToInject)
		    }
	     	
	        return instance;
	    } catch (InstantiationException e) {
	        throw new GrailsDomainException("Error instantiating constraint [" + 
	        		constraintClass + "] during validation: " + e.getMessage(), e );
	    } catch (IllegalAccessException e) {
	        throw new GrailsDomainException("Error instantiating constraint [" + 
	        		constraintClass + "] during validation: " + e.getMessage(), e );
	    }
	}
}
