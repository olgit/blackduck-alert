import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import ConfigButtons from 'distribution/job/BaseJobConfiguration';
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import { deleteDistributionJob } from 'store/actions/distributions';

class JobDeleteModal extends Component {
    constructor(props) {
        super(props);
        this.onSubmit = this.onSubmit.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.state = {
            inProgress: false
        };
    }


    onSubmit(event) {
        event.preventDefault();
        this.setState({
            inProgress: true
        });
        this.props.jobs.forEach((job) => {
            this.props.deleteDistributionJob(job);
        });
        this.props.onModalSubmit();
        this.setState({
            inProgress: false
        });
        if (!this.props.jobConfigTableMessage) {
            this.props.onModalClose();
        }
    }

    handleClose() {
        this.props.onModalClose();
    }

    render() {
        const tableData = this.props.createTableData(this.props.jobs);
        const jobTableOptions = {
            noDataText: 'No jobs configured'
        };
        return (
            <Modal size="lg" show={this.props.show} onHide={this.handleClose}>
                <Modal.Header closeButton>
                    <Modal.Title>Are you sure you want to delete these jobs?</Modal.Title>
                </Modal.Header>

                <Modal.Body>
                    <form className="form-horizontal" onSubmit={this.onSubmit}>
                        <div className="form-group">
                            <BootstrapTable
                                version="4"
                                hover
                                condensed
                                data={tableData}
                                options={jobTableOptions}
                                containerClass="table"
                                trClassName="tableRow"
                                headerContainerClass="scrollable"
                                bodyContainerClass="tableScrollableBody"
                            >
                                <TableHeaderColumn dataField="id" isKey hidden>Job Id</TableHeaderColumn>
                                <TableHeaderColumn dataField="distributionConfigId" hidden>Distribution Id</TableHeaderColumn>
                                <TableHeaderColumn dataField="name" dataSort columnTitle columnClassName="tableCell">Distribution Job</TableHeaderColumn>
                                <TableHeaderColumn dataField="distributionType" dataSort columnClassName="tableCell" dataFormat={this.props.typeColumnDataFormat}>Type</TableHeaderColumn>
                                <TableHeaderColumn dataField="providerName" dataSort columnClassName="tableCell" dataFormat={this.props.providerColumnDataFormat}>Provider</TableHeaderColumn>
                                <TableHeaderColumn dataField="frequency" dataSort columnClassName="tableCell" dataFormat={this.props.frequencyColumnDataFormat}>Frequency Type</TableHeaderColumn>
                                <TableHeaderColumn dataField="lastRan" dataSort columnTitle columnClassName="tableCell">Last Run</TableHeaderColumn>
                                <TableHeaderColumn dataField="status" dataSort columnTitle columnClassName={this.props.statusColumnClassNameFormat}>Status</TableHeaderColumn>
                            </BootstrapTable>
                        </div>
                        <p name="jobConfigTableMessage">{this.props.jobConfigTableMessage}</p>
                        <ConfigButtons performingAction={this.state.inProgress} cancelId="job-cancel" submitId="job-submit" submitLabel="Confirm" includeSave includeCancel onCancelClick={this.handleClose} />
                    </form>
                </Modal.Body>

            </Modal>
        );
    }
}

JobDeleteModal.propTypes = {
    createTableData: PropTypes.func.isRequired,
    jobConfigTableMessage: PropTypes.string.isRequired,
    onModalClose: PropTypes.func.isRequired,
    onModalSubmit: PropTypes.func.isRequired,
    deleteDistributionJob: PropTypes.func.isRequired,
    typeColumnDataFormat: PropTypes.func.isRequired,
    providerColumnDataFormat: PropTypes.func.isRequired,
    frequencyColumnDataFormat: PropTypes.func.isRequired,
    statusColumnClassNameFormat: PropTypes.func.isRequired,
    jobs: PropTypes.arrayOf(PropTypes.object),
    show: PropTypes.bool
};

JobDeleteModal.defaultProps = {
    jobs: [],
    show: false
};

const mapStateToProps = state => ({
    jobConfigTableMessage: state.distributions.jobConfigTableMessage
});

const mapDispatchToProps = dispatch => ({
    deleteDistributionJob: job => dispatch(deleteDistributionJob(job))
});

export default connect(mapStateToProps, mapDispatchToProps)(JobDeleteModal);
