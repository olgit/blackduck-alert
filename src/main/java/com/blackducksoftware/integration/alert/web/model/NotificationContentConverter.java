package com.blackducksoftware.integration.alert.web.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.descriptor.DatabaseContentConverter;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;

@Component
public class NotificationContentConverter extends DatabaseContentConverter {

    @Autowired
    public NotificationContentConverter(final ContentConverter contentConverter) {
        super(contentConverter);
        // TODO Auto-generated constructor stub
    }

    @Override
    public ConfigRestModel getRestModelFromJson(final String json) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DatabaseEntity populateDatabaseEntityFromRestModel(final ConfigRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConfigRestModel populateRestModelFromDatabaseEntity(final DatabaseEntity entity) {
        // TODO Auto-generated method stub
        return null;
    }

}
