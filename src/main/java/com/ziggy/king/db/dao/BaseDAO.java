package com.ziggy.king.db.dao;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import com.ziggy.king.properties.DatabaseProperties;

public class BaseDAO {
	private static SessionFactory sessionFactory;
	private static SessionFactory defaultFactory;
	protected Session session;

	public BaseDAO() {
		session = getSessionFactory().getCurrentSession();
	}
	
	public void saveOrUpdate(Object entity) {
		session.saveOrUpdate(entity);
	}

	public void saveEntity(Object entity) {
		session.save(entity);
	}

	public void refreshEntity(Object entity) {
		session.refresh(entity);
	}

	public void deleteEntity(Object object) {
		session.delete(object);
	}

	public BaseDAO(Session session) {
		if (session != null) {
			this.session = session;
		} else {
			this.session = getSessionFactory().getCurrentSession();
		}
	}

	public synchronized static void init() {
		if (sessionFactory == null) {
			Configuration configuration = new Configuration();
			configuration.configure();
			configuration.setProperty("hibernate.connection.driver_class", DatabaseProperties.getProperties().getDatabaseDriver());
			configuration.setProperty("hibernate.connection.url", DatabaseProperties.getProperties().getDatabaseUrl());
			configuration.setProperty("hibernate.connection.username", DatabaseProperties.getProperties().getDatabaseUsername());
			configuration.setProperty("hibernate.connection.password", DatabaseProperties.getProperties().getDatabasePassword());
			configuration.setProperty("hibernate.show_sql", DatabaseProperties.getProperties().getShowSQL());
			ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
			defaultFactory = configuration.buildSessionFactory(serviceRegistry);
			sessionFactory = defaultFactory;
		}
	}

	public synchronized static void changeDatabase(String databaseUrl) {
		Configuration configuration = new Configuration();
		configuration.configure();
		configuration.setProperty("hibernate.connection.driver_class", DatabaseProperties.getProperties().getDatabaseDriver());
		configuration.setProperty("hibernate.connection.url", databaseUrl);
		configuration.setProperty("hibernate.connection.username", DatabaseProperties.getProperties().getDatabaseUsername());
		configuration.setProperty("hibernate.connection.password", DatabaseProperties.getProperties().getDatabasePassword());
		configuration.setProperty("hibernate.show_sql", DatabaseProperties.getProperties().getShowSQL());
		ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
		defaultFactory = configuration.buildSessionFactory(serviceRegistry);
		sessionFactory = defaultFactory;
	}

	public static SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			init();
		}
		return sessionFactory;
	}

	public static void beginTransaction() {
		// logger.debug("Opening transaction for database " + getURL());
		getSessionFactory().getCurrentSession().beginTransaction();
	}

	public static void endTransaction() {
		getSessionFactory().getCurrentSession().getTransaction().commit();
		getSessionFactory().getCurrentSession().close();
	}

	public static void rollbackTransaction() {
		getSessionFactory().getCurrentSession().getTransaction().rollback();
		getSessionFactory().getCurrentSession().close();
	}

	public static void flushTransaction() {
		getSessionFactory().getCurrentSession().flush();
	}

	public static String getURL() {
		try {
			DatabaseMetaData md = ((SessionFactoryImpl) getSessionFactory()).getConnectionProvider().getConnection().getMetaData();
			return md.getURL();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
