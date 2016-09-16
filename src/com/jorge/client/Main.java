package com.jorge.client;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.jorge.entity.Guide;

/**
 * Caching and object identity:
 * 
 * Caching is ID based (ID of the object)
 * 
 *
 */
public class Main {

	public static void main(String[] args) {
		BasicConfigurator.configure(); // Necessary for configure log4j. It must be the first line in main method
	       					           // log4j.properties must be in /src directory

		Logger  logger = Logger.getLogger(Main.class.getName());
		logger.debug("log4j configured correctly and logger set");

		logger.debug("creating entity manager factory");
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("helloworld");  
		
		logger.debug("creating entity manager em");
		EntityManager em = emf.createEntityManager(); // Entity Manager represents first-level cache. Entity Manager keeps in JVM (memory)
		
		logger.debug("getting em transaction and beginning em transaction");
		em.getTransaction().begin(); 
		
		logger.debug("getting guide info");
		Guide guide1 = em.find(Guide.class, 1L); // Row 1. When it wants to load this guide info (row with id 1 in guide table), Hibernate is going to look for it ti first-level cache
												 // If it is not found this info at the first-level cache, Hibernate executes a select statement (SQL at runtime) to get guide info from DB.
												 // This info returned by DB will be managed by Entity Manager (em). Now guide info is in first-level cache
		
		Guide guide2 = em.find(Guide.class, 1L); // Row 1 too. When it executes this line, the guide info is available in first-level cache. Hibernate doesn't have to execute a new select statement to get info
												 // At this point, we have one instance of the Guide entity for an Entity Manager (em) to represent a particular database row.
												 // One instance (same instance) of Guide entity for guide1 and guide2 objects because if you update salary data from guide2 (guide2.setSalary(8000) and commit (em.getTransaction().commit();), 
												 // update is for DB and guide1. if not, if it was an instance of Guide entity for each guide object (one for guide1 and other for guide2), if we make an update in salary in
												 // guide2 and commit, what happens with the Guide entity instance of guide1 object??? It's why there is an instance of Guide entity for all guide objects
		
		// If User1 executes until Guide guide2 = em.find(Guide.class, 1L); including, getting a salary value of 4000, and then User2 updates salary value to 2500 in DB. Then User1 executes: 
		// 		Guide guide3 = (Guide)em.createQuery("select guide form Guide guide where guide.id = :id").setParameter("id", 1L).getSingleResult();
		// What value will the salary of the instance of Guide entity in first-level cache of User1 have?
		// Answer is 4000 => REPEATABLE_READ
		Guide guide3 = (Guide)em.createQuery("select guide form Guide guide where guide.id = :id") // Hibernate executes a new select SQL statement (SQL at runtime), doesn't get info from first-level cache (EntityManager). 
																								   // It is because of a cached is ID based. Hibernate to be able to look for an object in a cache, it needs to know the ID of that object
																								   // After that, DB returns the guide info and now Hibernate knows that guide info is in first-level cache so guide3 points
																								   // to our one instance of the Guide entity
					   .setParameter("id", 1L)
					   .getSingleResult();
		
		logger.debug("making em commit");
		em.getTransaction().commit();
		
		logger.debug("close em entity manager");
		em.close();
		
	}

}
  