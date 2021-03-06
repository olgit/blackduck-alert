import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import MainPage from 'MainPage';
import LoginPage from 'LoginPage';
import AboutInfoFooter from 'component/AboutInfoFooter';
import SetupPage from 'SetupPage';
import { verifyLogin, verifySaml } from 'store/actions/session';
import { getInitialSystemSetup } from 'store/actions/system';
import * as IconUtility from 'util/iconUtility';
import LogoutPage from 'LogoutPage';

// These are needed for the react-bootstrap tables to show the ascending/descending icons
import '@fortawesome/fontawesome-free/scss/fontawesome.scss';
import '@fortawesome/fontawesome-free/js/all.js';
import '@fortawesome/fontawesome-free/scss/v4-shims.scss';
import '@fortawesome/fontawesome-free/js/v4-shims.js';

import '../css/main.scss';

IconUtility.loadIconData();

class App extends Component {
    componentDidMount() {
        this.props.getSettings();
        this.props.verifyLogin();
        this.props.verifySaml();
    }

    componentDidUpdate(prevProps) {
        const { systemInitialized, logoutPerformed, loggedIn, samlEnabled } = this.props;
        if (systemInitialized && !prevProps.systemInitialized
            && !logoutPerformed && !loggedIn && samlEnabled) {
            // Switching from un-initialized to initialized due to system setup.
            // Reload the page to display the correct login screen
            window.location.reload();
        }
    }

    render() {
        if (this.props.initializing) {
            return (<div />);
        }

        if (this.props.logoutPerformed) {
            return <LogoutPage />;
        }

        let contentPage = (this.props.loggedIn || this.props.samlEnabled) ? <MainPage /> : <LoginPage />;
        if (!this.props.systemInitialized) {
            contentPage = <SetupPage />;
        }

        return (
            <div>
                {contentPage}
                <AboutInfoFooter />
            </div>
        );
    }
}

App.propTypes = {
    loggedIn: PropTypes.bool.isRequired,
    logoutPerformed: PropTypes.bool.isRequired,
    initializing: PropTypes.bool.isRequired,
    verifyLogin: PropTypes.func.isRequired,
    verifySaml: PropTypes.func.isRequired,
    getSettings: PropTypes.func.isRequired,
    systemInitialized: PropTypes.bool.isRequired,
    samlEnabled: PropTypes.bool.isRequired
};

// Redux mappings to be used later....
const mapStateToProps = state => ({
    loggedIn: state.session.loggedIn,
    logoutPerformed: state.session.logoutPerformed,
    initializing: state.session.initializing,
    samlEnabled: state.session.samlEnabled,
    systemInitialized: state.system.systemInitialized
});

const mapDispatchToProps = dispatch => ({
    verifyLogin: () => dispatch(verifyLogin()),
    verifySaml: () => dispatch(verifySaml()),
    getSettings: () => dispatch(getInitialSystemSetup())
});

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(App));
