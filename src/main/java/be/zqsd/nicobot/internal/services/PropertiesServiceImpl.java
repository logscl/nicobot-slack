package be.zqsd.nicobot.internal.services;

import be.zqsd.nicobot.utils.NicobotProperty;
import be.zqsd.nicobot.services.PropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class PropertiesServiceImpl implements PropertiesService {

	@Autowired
	private Environment properties;
	
	@Override
	public String get(NicobotProperty key) {
		return properties.getProperty(key.getKey(), key.getDefaultValue());
	}
	
	@Override
	public Boolean getBoolean(NicobotProperty key) {
		//TODO si la clé est différente de "true" ou "false", il faudrait tester la default value au lieu de retourner false par défaut.
		return Boolean.valueOf(this.get(key));
	}
	
	@Override
	public Long getLong(NicobotProperty key) {
		//TODO check NumberFormatException pour la clé et la defaultValue
		return Long.valueOf(this.get(key));
	}
}
