package com.st.nicobot.services;

import com.st.nicobot.bot.utils.NicobotProperty;

public interface PropertiesService {

    /**
     * Récupère une valeur dans le fichier de properties
     * @param key la clé à chercher
     * @return la valeur liée à la clé
     */
    String get(NicobotProperty key);

    /**
     * Retourne un booléen suivant la valeur d'une clé du fichier de properties
     * @param key la valeur à chercher
     * @return <code>true</code> si la valeur de clé (ou defaultValue) = "<code>true</code>", false sinon
     */
    Boolean getBoolean(NicobotProperty key);

    Long getLong(NicobotProperty key);

}
