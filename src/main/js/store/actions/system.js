import {
    SYSTEM_LATEST_MESSAGES_FETCHED,
    SYSTEM_LATEST_MESSAGES_FETCHING,
    SYSTEM_SETUP_DESCRIPTOR_FETCHED,
    SYSTEM_SETUP_DESCRIPTOR_FETCHING,
    SYSTEM_SETUP_FETCH_ERROR,
    SYSTEM_SETUP_FETCH_REDIRECTED,
    SYSTEM_SETUP_FETCHING,
    SYSTEM_SETUP_HIDE_RESET_PASSWORD_MODAL,
    SYSTEM_SETUP_PASSWORD_RESETTING,
    SYSTEM_SETUP_SHOW_CONFIG,
    SYSTEM_SETUP_SHOW_RESET_PASSWORD_MODAL,
    SYSTEM_SETUP_UPDATE_ERROR,
    SYSTEM_SETUP_UPDATED,
    SYSTEM_SETUP_UPDATING
} from 'store/actions/types';
import { clearLoginError, loginError, verifyLoginByStatus } from 'store/actions/session';

const LATEST_MESSAGES_URL = '/alert/api/system/messages/latest';
const INITIAL_SYSTEM_SETUP_URL = '/alert/api/system/setup/initial';
const SYSTEM_DESCRIPTOR_URL = '/alert/api/system/setup/descriptor';

function fetchingLatestSystemMessages() {
    return {
        type: SYSTEM_LATEST_MESSAGES_FETCHING
    };
}

/**
 * Triggers Confirm config was fetched
 * @returns {{type}}
 */
function latestSystemMessagesFetched(latestMessages) {
    return {
        type: SYSTEM_LATEST_MESSAGES_FETCHED,
        latestMessages
    };
}

function fetchingSystemDescriptor() {
    return {
        type: SYSTEM_SETUP_DESCRIPTOR_FETCHING
    };
}

function systemDescriptorFetched(settingsDescriptor) {
    return {
        type: SYSTEM_SETUP_DESCRIPTOR_FETCHED,
        settingsDescriptor
    };
}

function fetchingSystemSetup() {
    return {
        type: SYSTEM_SETUP_FETCHING
    };
}

function fetchSetupRedirected() {
    return {
        type: SYSTEM_SETUP_FETCH_REDIRECTED
    };
}

function systemSetupShowConfig(settingsData) {
    return {
        type: SYSTEM_SETUP_SHOW_CONFIG,
        settingsData
    };
}

function systemSetupFetchError(message) {
    return {
        type: SYSTEM_SETUP_FETCH_ERROR,
        message
    };
}

function updatingSystemSetup() {
    return {
        type: SYSTEM_SETUP_UPDATING
    };
}

function systemSetupUpdated() {
    return {
        type: SYSTEM_SETUP_UPDATED
    };
}

function systemSetupUpdateError(message, errors) {
    return {
        type: SYSTEM_SETUP_UPDATE_ERROR,
        message,
        errors
    };
}

function resettingPassword() {
    return {
        type: SYSTEM_SETUP_PASSWORD_RESETTING
    };
}

export function showResetModal() {
    return {
        type: SYSTEM_SETUP_SHOW_RESET_PASSWORD_MODAL
    };
}

export function hideResetModal() {
    return {
        type: SYSTEM_SETUP_HIDE_RESET_PASSWORD_MODAL
    };
}

export function getLatestMessages() {
    return (dispatch) => {
        dispatch(fetchingLatestSystemMessages());
        fetch(LATEST_MESSAGES_URL)
            .then((response) => {
                if (response.ok) {
                    response.json()
                        .then((body) => {
                            dispatch(latestSystemMessagesFetched(body));
                        });
                } else {
                    dispatch(verifyLoginByStatus(response.status));
                }
            })
            .catch(console.error);
    };
}

export function getInitialSystemSetup() {
    return (dispatch) => {
        dispatch(fetchingSystemSetup());
        fetch(INITIAL_SYSTEM_SETUP_URL)
            .then((response) => {
                if (response.redirected) {
                    dispatch(fetchSetupRedirected());
                } else if (response.ok) {
                    response.json()
                        .then((body) => {
                            dispatch(systemSetupShowConfig(body));
                        });
                } else {
                    dispatch(systemSetupFetchError(response.statusText));
                }
            })
            .catch(console.error);
    };
}

export function getInitialSystemDescriptor() {
    return (dispatch) => {
        dispatch(fetchingSystemDescriptor());
        fetch(SYSTEM_DESCRIPTOR_URL)
            .then((response) => {
                if (response.redirected) {
                    dispatch(fetchSetupRedirected());
                } else if (response.ok) {
                    response.json()
                        .then((body) => {
                            dispatch(systemDescriptorFetched(body));
                        });
                } else {
                    dispatch(systemSetupFetchError(response.statusText));
                }
            })
            .catch(console.error);
    };
}

export function saveInitialSystemSetup(setupData) {
    return (dispatch, getState) => {
        dispatch(updatingSystemSetup());
        const { csrfToken } = getState().session;
        const options = {
            credentials: 'same-origin',
            method: 'POST',
            body: JSON.stringify(setupData),
            headers: {
                'content-type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            }
        };

        fetch(INITIAL_SYSTEM_SETUP_URL, options)
            .then((response) => {
                if (response.ok) {
                    dispatch(systemSetupUpdated());
                    dispatch(getInitialSystemSetup());
                } else {
                    response.json()
                        .then((body) => {
                            const jsonErrors = body.errors;
                            const errors = {};
                            if (jsonErrors) {
                                Object.keys(jsonErrors)
                                    .forEach((key) => {
                                        if (jsonErrors[key]) {
                                            const value = jsonErrors[key];
                                            errors[key] = value;
                                        }
                                    });
                            }
                            dispatch(systemSetupUpdateError(body.message, errors));
                        });
                }
            })
            .catch(console.error);
    };
}

export function sendPasswordResetEmail(username) {
    return (dispatch) => {
        dispatch(clearLoginError());
        dispatch(resettingPassword());
        const url = `/alert/api/resetPassword/${encodeURIComponent(username)}`;
        fetch(url, {
            method: 'POST'
        })
            .then((response) => {
                dispatch(hideResetModal());
                if (!response.ok) {
                    response.json()
                        .then(body =>
                            dispatch(loginError(body.message, body.fieldErrors)));
                }
            })
            .catch(console.error);
    };
}
