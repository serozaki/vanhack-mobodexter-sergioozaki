import React, { Component } from 'react';
import { Button, Alert, Container, Form, FormGroup, Label, Input } from 'reactstrap';
import AppNavbar from './AppNavbar';
import { Link } from 'react-router-dom';


function TestResult(props) {
  const { showResult, testSuccess, testMessage, hideMessage } = props;
  if (testSuccess === true) {
    return <Alert color="success" isOpen={showResult} toggle={() => hideMessage()}>
      Success!
  </Alert>;
  } else {
    return <Alert color="danger" isOpen={showResult} toggle={() => hideMessage()}>
      Error: {testMessage}
    </Alert>;
  }
}

class TestConnection extends Component {

  constructor(props) {
    super(props);
    this.state = {
      configs: [],
      isLoading: true,
      connectionId: null,
      payload: '',
      showResult: false,
      testSuccess: true,
      testMessage: ''
    };
    this.testConnection = this.testConnection.bind(this);
    this.hideMessage = this.hideMessage.bind(this);
  }

  componentDidMount() {
    this.setState({ isLoading: true });

    fetch('api/configs')
      .then(response => response.json())
      .then(data => this.setState({ configs: data, isLoading: false, connectionId: data[0] && data[0].id || '' }));
  }

  showMessage(success, message = '') {
    this.setState({ showResult: true, testSuccess: success, testMessage: message });

  }
  hideMessage() {
    this.setState({ showResult: false });
  }

  async testConnection(event) {
    event.preventDefault();
    const { connectionId, payload } = this.state;
    console.log({connectionId})
    let msg = '';
    let success = false;
    await fetch('/api/test/publishTest/' + connectionId , {
      method: 'POST',
      body: JSON.stringify({ payload })
    }).then(response => {
      if (response.ok) {
        success = true;
        return null;
      }
      msg += response.status;
      return response.text();
    }).then(data => msg += ' ' + data).catch(e => {
      console.error(e);
      msg += ' Exception';
    });
    this.showMessage(success, msg);
  }

  render() {
    const { configs, isLoading, connectionId } = this.state;
    const { showResult, testSuccess, testMessage } = this.state;

    if (isLoading) {
      return <p>Loading...</p>;
    }

    const configList = configs.map(config => {
      return <option key={config.id} value={config.id} >{config.id}: {config.name} ({config.type})</option>
    });

    return (
      <div>
        <AppNavbar />
        <Container fluid>
          <h3>IoT Cloud Connection</h3>
          <TestResult showResult={showResult} testSuccess={testSuccess} testMessage={testMessage} hideMessage={this.hideMessage} />
          <Form onSubmit={this.testConnection}><FormGroup>
            <Label for="type">Cloud Provider</Label>
            <Input type="select" name="type" id="type"
              onChange={(event) => this.setState({ connectionId: event.target.value })}
              value={connectionId} >
              {configList}
            </Input>
          </FormGroup>
            <FormGroup>
              <Label for="payload">Message</Label>
              <Input type="text" name="payload" id="payload"
                onChange={(event) => this.setState({ payload: event.target.value })} />
            </FormGroup>
            <FormGroup >
              <Button color="success" type="submit">Publish message</Button>
            </FormGroup>
          </Form>
        </Container>
      </div>
    );
  }
}

export default TestConnection;
