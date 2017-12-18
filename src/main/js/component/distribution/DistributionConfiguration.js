import React, { Component } from 'react';

import styles from '../../../css/distributionConfig.css';

import tableStyles from '../../../css/table.css';

import GroupEmailJobConfiguration from './job/GroupEmailJobConfiguration';
import HipChatJobConfiguration from './job/HipChatJobConfiguration';
import SlackJobConfiguration from './job/SlackJobConfiguration';
import EditTableCellFormatter from '../EditTableCellFormatter';

import JobAddModal from './JobAddModal';

import {ReactBsTable, BootstrapTable, TableHeaderColumn, InsertButton, DeleteButton} from 'react-bootstrap-table';
import 'react-bootstrap-table/dist/react-bootstrap-table-all.min.css';

class DistributionConfiguration extends Component {
	constructor(props) {
		super(props);
		 this.state = {
			configurationMessage: '',
			errors: {},
			jobs: [],
			projects: [],
			groups: [],
			waitingForProjects: true,
			waitingForGroups: true
		};
		this.createCustomModal = this.createCustomModal.bind(this);
		this.createCustomDeleteButton = this.createCustomDeleteButton.bind(this);
		this.createCustomInsertButton = this.createCustomInsertButton.bind(this);
		this.cancelRowSelect = this.cancelRowSelect.bind(this);
		this.editButtonClicked = this.editButtonClicked.bind(this);
        this.editButtonClick = this.editButtonClick.bind(this);
        this.handleSetState = this.handleSetState.bind(this);
        this.customJobConfigDeletionConfirm = this.customJobConfigDeletionConfirm.bind(this);
	}

    addDefaultJobs() {
        const { jobs } = this.state;
        jobs.push({
            distributionConfigId: '999',
            name: 'Test Job',
            distributionType: 'email_group_channel',
            lastRun: '12/01/2017 00:00:00',
            status: 'Success',
            frequency: 'DAILY',
            notificationTypes: [
            'POLICY_VIOLATION',
            'POLICY_VIOLATION_CLEARED',
            'POLICY_VIOLATION_OVERRIDE'],
            groupName: 'Custom Group',
            configuredProjects: ['PSTestApp']
        });
        jobs.push({
            distributionConfigId: '998',
            name: 'Alert Slack Job',
            distributionType: 'slack_channel',
            lastRun: '12/02/2017 00:00:00',
            status: 'Failure',
            frequency: 'REAL_TIME',
            notificationTypes: [
            'POLICY_VIOLATION_OVERRIDE',
            'HIGH_VULNERABILITY'],
            configuredProjects: ['missing-1', 'missing-2']
        });
        jobs.push({
            distributionConfigId: '997',
            name: 'HipChat Job',
            distributionType: 'hipchat_channel',
            lastRun: '1/01/2017 00:00:00',
            status: 'Success',
            frequency: 'DAILY',
            notificationTypes: [
            'POLICY_VIOLATION',
            'POLICY_VIOLATION_CLEARED',
            'POLICY_VIOLATION_OVERRIDE',
            'HIGH_VULNERABILITY',
            'MEDIUM_VULNERABILITY',
            'LOW_VULNERABILITY'],
            includeAllProjects: true,
            configuredProjects: []
        });
    }

	componentDidMount() {
		var self = this;

		fetch('/hub/projects',{
			credentials: "same-origin"
		})
		.then(function(response) {
			self.handleSetState('waitingForProjects', false);
			if (!response.ok) {
				return response.json().then(json => {
					self.handleSetState('projectTableMessage', json.message);
				});
			} else {
				return response.json().then(json => {
					self.handleSetState('projectTableMessage', '');
					var jsonArray = JSON.parse(json.message);
					if (jsonArray != null && jsonArray.length > 0) {
						var projects = [];
						for (var index in jsonArray) {
							projects.push({
								name: jsonArray[index].name,
								url: jsonArray[index].url
							});
						}
						self.setState({
							projects
						});
					}
				});
			}
		});

		fetch('/hub/groups',{
			credentials: "same-origin",
		})
		.then(function(response) {
			self.handleSetState('waitingForGroups', false);
			if (!response.ok) {
				return response.json().then(json => {
					self.handleSetState('groupError', json.message);
				});
			} else {
				return response.json().then(json => {
					self.handleSetState('groupError', '');
					var jsonArray = JSON.parse(json.message);
					if (jsonArray != null && jsonArray.length > 0) {
						var groups = [];
						for (var index in jsonArray) {
							groups.push({
								name: jsonArray[index].name,
								active: jsonArray[index].active,
								url: jsonArray[index].url
							});
						}
						self.setState({
							groups
						});
					}
				});
			}
		});
        this.fetchDistributionJobs();
    }

    updateJobsTable() {
        //TODO remove this and references to it
        //this.fetchDistributionJobs();
    }

    fetchDistributionJobs() {
        let self = this;
        fetch('/configuration/distribution/common',{
			credentials: "same-origin",
            headers: {
				'Content-Type': 'application/json'
			}
		})
		.then(function(response) {
			if (response.ok) {
                response.json().then(jsonArray => {
                    let newJobs = new Array();
                    self.addDefaultJobs();
					if (jsonArray != null && jsonArray.length > 0) {
                        jsonArray.forEach((item) =>{
                            let jobConfig = {
                            	id: item.id,
                                distributionConfigId: item.distributionConfigId,
                    			name: item.name,
                    			distributionType: item.distributionType,
                    			lastRun: '',
                    			status: '',
                                frequency: item.frequency,
                                notificationTypes: item.notificationTypes,
                                configuredProjects: item.configuredProjects
                            };

                            newJobs.push(jobConfig);
                        });
                    }
                   self.setState({
						jobs: newJobs
					});
                });
            } else {
				return response.json().then(json => {
					let jsonErrors = json.errors;
					if (jsonErrors) {
						var errors = {};
						for (var key in jsonErrors) {
							if (jsonErrors.hasOwnProperty(key)) {
								let name = key.concat('Error');
								let value = jsonErrors[key];
								errors[name] = value;
							}
						}
						self.setState({
							errors
						});
					}
					self.setState({
						jobConfigTableMessage: json.message
					});
				});
            }
        });
    }

    statusColumnClassNameFormat(fieldValue, row, rowIdx, colIdx) {
		var className = tableStyles.statusSuccess;
		if (fieldValue === 'Failure') {
			className = tableStyles.statusFailure;
		}
		return className;
	}

	typeColumnDataFormat(cell, row) {
		let fontAwesomeClass = "";
        let cellText = '';
		if (cell === 'email_group_channel') {
			fontAwesomeClass = 'fa fa-envelope';
            cellText = "Group Email";
		} else if (cell === 'hipchat_channel') {
			fontAwesomeClass = 'fa fa-comments';
            cellText = "HipChat";
		} else if (cell === 'slack_channel') {
			fontAwesomeClass = 'fa fa-slack';
            cellText = "Slack";
		}

		let data = <div>
						<i key="icon" className={fontAwesomeClass} aria-hidden='true'></i>
						{cellText}
					</div>;

		return data;
	}

    createCustomModal(onModalClose, onSave, columns, validateState, ignoreEditable) {
        return (
	    	<JobAddModal
	    		waitingForProjects={this.state.waitingForProjects}
	    		waitingForGroups={this.state.waitingForGroups}
	    		projects={this.state.projects}
	    		includeAllProjects={true}
	    		groups={this.state.groups}
	    		groupError={this.state.groupError}
	    		projectTableMessage={this.state.projectTableMessage}
	    		handleCancel={this.cancelRowSelect}
                updateJobsTable={this.updateJobsTable}
		    	onModalClose= { onModalClose }
		    	onSave= { onSave }
		    	columns={ columns }
		        validateState={ validateState }
		        ignoreEditable={ ignoreEditable } />
	    );
	}

	customJobConfigDeletionConfirm(next, dropRowKeys) {
	  if (confirm("Are you sure you want to delete these Job configurations?")) {
	  	console.log('Deleting the Job configs');
	  	//TODO delete the Job configs from the backend
	  	// dropRowKeys are the Id's of the Job configs
		let self = this;
		var jobs = self.state.jobs;

		var matchingJobs = jobs.filter(job => {
			return dropRowKeys.includes(job.id);
		});
	  	matchingJobs.forEach(function(job){
	  		let jsonBody = JSON.stringify(job);
		    fetch('/configuration/distribution/common',{
		    	method: 'DELETE',
				credentials: "same-origin",
	            headers: {
					'Content-Type': 'application/json'
				},
				body: jsonBody
			}).then(function(response) {
				if (!response.ok) {
					return response.json().then(json => {
					let jsonErrors = json.errors;
					if (jsonErrors) {
						var errors = {};
						for (var key in jsonErrors) {
							if (jsonErrors.hasOwnProperty(key)) {
								let name = key.concat('Error');
								let value = jsonErrors[key];
								errors[name] = value;
							}
						}
						self.setState({
							errors
						});
					}
					self.setState({
						jobConfigTableMessage: json.message
					});
				});
				}
			});
		});
	  	next();
	  }
	}

	createCustomDeleteButton(onClick) {
		return (
			<DeleteButton
			className={tableStyles.deleteJobButton}/>
		);
	}

	createCustomInsertButton(onClick) {
		return (
			<InsertButton
			className={tableStyles.addJobButton}
			/>
		);
	}

	handleSetState(name, value) {
		this.setState({
			[name]: value
		});
	}

	cancelRowSelect() {
		this.setState({
			currentRowSelected: null
		});
	}

	getCurrentJobConfig(currentRowSelected) {
		let currentJobConfig = null;
		if (currentRowSelected != null) {
            const { id, name, distributionConfigId, distributionType, frequency, notificationTypes, groupName, includeAllProjects, configuredProjects } = currentRowSelected;
			if (distributionType === 'email_group_channel') {
				currentJobConfig = <GroupEmailJobConfiguration buttonsFixed={true} id={id} distributionConfigId={distributionConfigId} name={name} includeAllProjects={includeAllProjects} frequency={frequency} notificationTypes={notificationTypes} waitingForGroups={this.state.waitingForGroups} groups={this.state.groups} groupName={groupName} waitingForProjects={this.state.waitingForProjects} projects={this.state.projects} configuredProjects={configuredProjects} handleCancel={this.cancelRowSelect} updateJobsTable={this.updateJobsTable} projectTableMessage={this.state.projectTableMessage} />;
			} else if (distributionType === 'hipchat_channel') {
				currentJobConfig = <HipChatJobConfiguration buttonsFixed={true} id={id} distributionConfigId={distributionConfigId} name={name} includeAllProjects={includeAllProjects} frequency={frequency} notificationTypes={notificationTypes} waitingForProjects={this.state.waitingForProjects} projects={this.state.projects} configuredProjects={configuredProjects} handleCancel={this.cancelRowSelect} projectTableMessage={this.state.projectTableMessage} updateJobsTable={this.updateJobsTable}/>;
			} else if (distributionType === 'slack_channel') {
				currentJobConfig = <SlackJobConfiguration buttonsFixed={true} id={id} distributionConfigId={distributionConfigId} name={name} includeAllProjects={includeAllProjects} frequency={frequency} notificationTypes={notificationTypes} waitingForProjects={this.state.waitingForProjects} projects={this.state.projects} configuredProjects={configuredProjects} handleCancel={this.cancelRowSelect} projectTableMessage={this.state.projectTableMessage} updateJobsTable={this.updateJobsTable}/>;
			}
		}
		return currentJobConfig;
	}

	editButtonClicked(currentRowSelected) {
		this.handleSetState('currentRowSelected', currentRowSelected);
	}
	
    editButtonClick(cell, row) {
        return <EditTableCellFormatter handleButtonClicked={this.editButtonClicked} currentRowSelected= {row} />;
    }


	render() {
		const jobTableOptions = {
	  		noDataText: 'No jobs configured',
	  		clearSearch: true,
	  		insertBtn: this.createCustomInsertButton,
	  		deleteBtn: this.createCustomDeleteButton,
	  		insertModal: this.createCustomModal,
	  		handleConfirmDeleteRow: this.customJobConfigDeletionConfirm
		};
		const jobsSelectRowProp = {
	  		mode: 'checkbox',
	  		clickToSelect: true,
			bgColor: function(row, isSelect) {
				if (isSelect) {
					return '#e8e8e8';
				}
				return null;
			}
		};
		var content = <div>
						<BootstrapTable data={this.state.jobs} containerClass={tableStyles.table} striped hover condensed insertRow={true} deleteRow={true} selectRow={jobsSelectRowProp} search={true} options={jobTableOptions} trClassName={tableStyles.tableRow} headerContainerClass={tableStyles.scrollable} bodyContainerClass={tableStyles.tableScrollableBody} >
	      					<TableHeaderColumn dataField='id' isKey hidden>Job Id</TableHeaderColumn>
	      					<TableHeaderColumn dataField='distributionConfigId' hidden>Distribution Id</TableHeaderColumn>
	      					<TableHeaderColumn dataField='name' dataSort>Distribution Job</TableHeaderColumn>
	      					<TableHeaderColumn dataField='distributionType' dataSort dataFormat={ this.typeColumnDataFormat }>Type</TableHeaderColumn>
	      					<TableHeaderColumn dataField='lastRun' dataSort>Last Run</TableHeaderColumn>
	      					<TableHeaderColumn dataField='status' dataSort columnClassName={ this.statusColumnClassNameFormat }>Status</TableHeaderColumn>
                            <TableHeaderColumn dataField='' dataFormat={ this.editButtonClick }></TableHeaderColumn>
	  					</BootstrapTable>
	  					<p name="jobConfigTableMessage">{this.state.jobConfigTableMessage}</p>
  					</div>;
		var currentJobContent = this.getCurrentJobConfig (this.state.currentRowSelected);
		if (currentJobContent != null) {
			content = currentJobContent;
		}
		return (
				<div>
					{content}
				</div>
		)
	}
};

export default DistributionConfiguration;
