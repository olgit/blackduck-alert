export const DESCRIPTOR_TYPE = {
    PROVIDER: 'PROVIDER',
    CHANNEL: 'CHANNEL',
    COMPONENT: 'COMPONENT'
};

export const CONTEXT_TYPE = {
    GLOBAL: 'GLOBAL',
    DISTRIBUTION: 'DISTRIBUTION'
};


export const DESCRIPTOR_NAME = {
    CHANNEL_EMAIL: 'channel_email',
    CHANNEL_SLACK: 'channel_slack',
    COMPONENT_AUDIT: 'component_audit',
    COMPONENT_SCHEDULING: 'component_scheduling',
    COMPONENT_SETTINGS: 'component_settings',
    PROVIDER_BLACKDUCK: 'provider_blackduck',
    PROVIDER_POLARIS: 'provider_polaris'
};

export const OPERATIONS = {
    CREATE: 'CREATE',
    DELETE: 'DELETE',
    READ: 'READ',
    WRITE: 'WRITE',
    EXECUTE: 'EXECUTE'
};

export function findDescriptorByNameAndContext(descriptorList, descriptorName, context) {
    if (!descriptorList) {
        return null;
    }
    const resultList = descriptorList.filter(descriptor => descriptor.name === descriptorName && descriptor.context === context);
    if (!resultList) {
        return null;
    }

    return resultList;
}

export function findDescriptorByTypeAndContext(descriptorList, descriptorType, context) {
    if (!descriptorList) {
        return null;
    }
    const resultList = descriptorList.filter(descriptor => descriptor.type === descriptorType && descriptor.context === context);
    if (!resultList) {
        return null;
    }

    return resultList;
}

export function findDescriptorField(descriptor, fieldKey) {
    if (descriptor) {
        const fieldArray = descriptor.fields;
        if (fieldArray) {
            return fieldArray.find(field => field.key === fieldKey);
        }
    }
    return null;
}

export function findDescriptorFieldOptions(descriptor, fieldKey) {
    const field = findDescriptorField(descriptor, fieldKey);
    if (field) {
        return field.options;
    }
    return [];
}

export function isOneOperationAssigned(descriptor, operationArray) {
    if (!operationArray) {
        return false;
    }
    return operationArray.find(operation => isOperationAssigned(descriptor, operation)) !== undefined;
}

export function isOperationAssigned(descriptor, operationName) {
    return descriptor.operations.find(operation => operation === operationName) !== undefined;
}

