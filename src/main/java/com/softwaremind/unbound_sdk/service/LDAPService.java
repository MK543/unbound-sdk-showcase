package com.softwaremind.unbound_sdk.service;

import com.unboundid.ldap.sdk.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LDAPService {

    private final LDAPConnectionPool connectionPool;

    public void createEntry(String dn, Map<String, Object> attributes) {
        List<Attribute> attrs = new ArrayList<>();

        if (attributes.containsKey("objectClass")) {
            Object value = attributes.get("objectClass");
            if (value instanceof Collection) {
                attrs.add(new Attribute("objectClass", (Collection<String>) value));
            } else {
                attrs.add(new Attribute("objectClass", value.toString()));
            }
        } else {
            throw new RuntimeException("Cannot find object class");
        }

        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String attrName = entry.getKey();
            if ("objectClass".equalsIgnoreCase(attrName)) {
                continue;
            }
            Object value = entry.getValue();
            if (value instanceof Collection) {
                attrs.add(new Attribute(attrName, (Collection<String>) value));
            } else {
                attrs.add(new Attribute(attrName, value.toString()));
            }
        }

        Entry entry = new Entry(dn, attrs);
        LDAPConnection connection = null;
        try {
            connection = connectionPool.getConnection();
            connection.add(entry);
            log.info("Entry created successfully with DN: {}", dn);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        finally {
            connectionPool.releaseConnection(connection);
        }
    }

    public Map<String, Object> getEntryAttributes(String dn, List<String> attributes) {
        LDAPConnection connection = null;
        try {
            connection = connectionPool.getConnection();
            DN baseDN = new DN(dn);
            SearchRequest searchRequest = new SearchRequest(
                    baseDN,
                    SearchScope.BASE,
                    Filter.createPresenceFilter("objectClass"),
                    attributes.toArray(new String[0])
            );
            SearchResult searchResult = connection.search(searchRequest);
            if (searchResult.getEntryCount() == 0) {
                return Collections.emptyMap();
            }
            Entry entry = searchResult.getSearchEntries().getFirst();
            return mapEntryToAttributes(entry);
        }catch (LDAPException e){
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        finally {
            connectionPool.releaseConnection(connection);
        }
    }

    private Map<String, Object> mapEntryToAttributes(Entry entry) {
        Map<String, Object> result = new HashMap<>();
        for (Attribute attribute : entry.getAttributes()) {
            String attrName = attribute.getName();
            String[] values = attribute.getValues();
            if (values.length == 1) {
                result.put(attrName, values[0]);
            } else {
                result.put(attrName, Arrays.asList(values));
            }
        }
        return result;
    }

    public void modifyEntry(String dn, Map<String, Object> attributesToModify) {
        List<Modification> modifications = new ArrayList<>();

        for (Map.Entry<String, Object> entry : attributesToModify.entrySet()) {
            String attrName = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                modifications.add(new Modification(ModificationType.DELETE, attrName));
            } else if (value instanceof Collection) {
                String[] values = ((Collection<?>) value).toArray(new String[0]);
                modifications.add(new Modification(ModificationType.REPLACE, attrName, values));
            } else {
                modifications.add(new Modification(ModificationType.REPLACE, attrName, value.toString()));
            }
        }

        LDAPConnection connection = null;

        try {
            connection = connectionPool.getConnection();
            ModifyRequest modifyRequest = new ModifyRequest(dn, modifications);
            connection.modify(modifyRequest);
            log.info("Entry modified successfully with DN: {}", dn);
        } catch (LDAPException e) {
            log.error("Error modifying entry", e);
        }
        finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
    }

    public void deleteEntry(String dn) {
        LDAPConnection connection = null;
        try {
            connection = connectionPool.getConnection();
            connection.delete(dn);
            log.info("Entry deleted successfully with DN: {}", dn);
        } catch (LDAPException e) {
            log.error("Error deleting entry", e);
        }
        finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
    }


}
