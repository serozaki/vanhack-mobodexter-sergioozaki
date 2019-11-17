import React, { Component } from 'react';
import { Link, withRouter } from 'react-router-dom';
import { Button, Container, Form, FormGroup, Input, Label } from 'reactstrap';
import AppNavbar from './AppNavbar';

class ConfigEdit extends Component {

  emptyItem = {
    name: '',
    type: 'AWS',
    properties: []
  };

  constructor(props) {
    super(props);
    this.state = {
      item: this.emptyItem,
      itemProperties: {}
    };
    this.handleChange = this.handleChange.bind(this);
    this.handleTypeChange = this.handleTypeChange.bind(this);
    this.handlePropertyChange = this.handlePropertyChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  async componentDidMount() {
    if (this.props.match.params.id !== 'new') {
      const config = await (await fetch(`/api/config/${this.props.match.params.id}`)).json();
      let properties = {};
      if (config.properties) {
        config.properties.reduce((prev, property) => {
          prev[property.name] = property;
          return prev;
        }, properties);
      };
      this.setState({ item: config, itemProperties: properties });
    }
  }

  handleChange(event) {
    const target = event.target;
    const value = target.value;
    const name = target.name;
    let item = { ...this.state.item };
    item[name] = value;
    this.setState({ item });
  }

  handleTypeChange(event) {
    this.handleChange(event)
    this.setState({ itemProperties: {} });
  }

  handlePropertyChange(event) {
    const target = event.target;
    const value = target.value;
    const name = target.name;
    let itemProperties = { ...this.state.itemProperties };
    if (!itemProperties[name]) {
      itemProperties[name] = { name, value };
    } else {
      itemProperties[name].name = name;
      itemProperties[name].value = value;
    }
    this.setState({ itemProperties });
  }

  async handleSubmit(event) {
    event.preventDefault();
    const { item, itemProperties } = this.state;
    item.properties = Object.values(itemProperties);

    await fetch((item.id) ? '/api/config/{item.id}' : '/api/config', {
      method: (item.id) ? 'PUT' : 'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(item),
    });
    this.props.history.push('/configs');
  }

  findProperty(itemProperties, name, defaultValue) {
    if (itemProperties[name] && itemProperties[name].value) {
      return itemProperties[name].value;
    }
    return defaultValue;
  }

  render() {
    const { item, itemProperties } = this.state;
    const title = <h2>{item.id ? 'Edit Config' : 'Add Config'}</h2>;

    let specificProperties;
    if (item.type === 'AWS') {
      specificProperties = <div>
        <FormGroup>
          <Label for="aws_endpoint">Endpoint</Label>
          <Input type="text" name="endpoint" id="aws_endpoint" value={this.findProperty(itemProperties, 'endpoint', '')}
            onChange={this.handlePropertyChange} autoComplete="aws_endpoint" />
        </FormGroup>
        <FormGroup>
          <Label for="aws_privateKey">Private key</Label>
          <Input type="textarea" name="privateKey" id="aws_privateKey" value={this.findProperty(itemProperties, 'privateKey', '')}
            onChange={this.handlePropertyChange} autoComplete="aws_privateKey" />
        </FormGroup>
        <FormGroup>
          <Label for="aws_certificate">Certificate</Label>
          <Input type="textarea" name="certificate" id="aws_certificate" value={this.findProperty(itemProperties, 'certificate', '')}
            onChange={this.handlePropertyChange} autoComplete="aws_certificate" />
        </FormGroup>
      </div>;
    } else if (item.type === 'GOOGLE_CLOUD') {
      specificProperties = <div>
        <FormGroup>
          <Label for="google_cloudRegion">Endpoint</Label>
          <Input type="text" name="cloudRegion" id="google_cloudRegion" value={this.findProperty(itemProperties, 'cloudRegion', '')}
            onChange={this.handlePropertyChange} autoComplete="google_cloudRegion" />
        </FormGroup>
        <FormGroup>
          <Label for="google_projectId">Project Id</Label>
          <Input type="text" name="projectId" id="google_projectId" value={this.findProperty(itemProperties, 'projectId', '')}
            onChange={this.handlePropertyChange} autoComplete="google_projectId" />
        </FormGroup>
        <FormGroup>
          <Label for="google_registryId">Registry Id</Label>
          <Input type="text" name="registryId" id="google_registryId" value={this.findProperty(itemProperties, 'registryId', '')}
            onChange={this.handlePropertyChange} autoComplete="google_registryId" />
        </FormGroup>
        <FormGroup>
          <Label for="google_algorithm">Algorithm</Label>
          <Input type="text" name="algorithm" id="google_algorithm" value={this.findProperty(itemProperties, 'algorithm', '')}
            onChange={this.handlePropertyChange} autoComplete="google_algorithm" />
        </FormGroup>
        <FormGroup>
          <Label for="google_privateKey">Private Key</Label>
          <Input type="textarea" name="privateKey" id="google_privateKey" value={this.findProperty(itemProperties, 'privateKey', '')}
            onChange={this.handlePropertyChange} autoComplete="google_privateKey" />
        </FormGroup>
      </div>;
    } else {
      specificProperties = <div></div>;
    }
    if (item)
      return <div>
        <AppNavbar />
        <Container>
          {title}
          <Form onSubmit={this.handleSubmit}>
            <FormGroup>
              <Label for="name" title="Same id of the IoT Cloud Provider">Client Id</Label>
              <Input type="text" name="name" id="name" value={item.name || ''}
                onChange={this.handleChange} autoComplete="name" />
            </FormGroup>
            <FormGroup>
              <Label for="type">Cloud Provider</Label>
              <Input type="select" name="type" id="type"
                onChange={this.handleTypeChange} value={item.type || ''} >
                <option value="AWS" >AWS</option>
                <option value="GOOGLE_CLOUD">GOOGLE_CLOUD</option>
              </Input>
            </FormGroup>
            {specificProperties}
            <FormGroup>
              <Button color="primary" type="submit">Save</Button>{' '}
              <Button color="secondary" tag={Link} to="/configs">Cancel</Button>
            </FormGroup>
          </Form>
        </Container>
      </div>
  }
}

export default withRouter(ConfigEdit);