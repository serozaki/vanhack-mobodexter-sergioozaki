import React, { Component } from 'react';
import { Button, ButtonGroup, Container, Table } from 'reactstrap';
import AppNavbar from './AppNavbar';
import { Link } from 'react-router-dom';

class ConfigList extends Component {

  constructor(props) {
    super(props);
    this.state = { configs: [], isLoading: true };
    this.remove = this.remove.bind(this);
  }

  componentDidMount() {
    this.setState({ isLoading: true });

    fetch('api/configs')
      .then(response => response.json())
      .then(data => this.setState({ configs: data, isLoading: false }));
  }

  async remove(id) {
    await fetch(`/api/config/${id}`, {
      method: 'DELETE',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      }
    }).then(() => {
      let updatedConfigs = [...this.state.configs].filter(i => i.id !== id);
      this.setState({ configs: updatedConfigs });
    });
  }

  render() {
    const { configs, isLoading } = this.state;

    if (isLoading) {
      return <p>Loading...</p>;
    }

    const configList = configs.map(config => {
      return <tr key={config.id}>
        <td>{config.id}</td>
        <td style={{ whiteSpace: 'nowrap' }}>{config.name}</td>
        <td>{config.type}</td>
        <td>
          <ButtonGroup>
            <Button size="sm" color="primary" tag={Link} to={"/configs/" + config.id}>Edit</Button>
            <Button size="sm" color="danger" onClick={() => this.remove(config.id)}>Delete</Button>
          </ButtonGroup>
        </td>
      </tr>
    });

    return (
      <div>
        <AppNavbar />
        <Container fluid>
          <div className="float-right">
            <Button color="success" tag={Link} to="/configs/new">Add Config</Button>
          </div>
          <h3>IoT Cloud Configuration List</h3>
          <Table className="mt-4">
            <thead>
              <tr>
                <th width="10%">Id</th>
                <th width="20%">Client Id</th>
                <th width="20%">Cloud Provider</th>
                <th width="10%">Actions</th>
              </tr>
            </thead>
            <tbody>
              {configList}
            </tbody>
          </Table>
        </Container>
      </div>
    );
  }
}

export default ConfigList;
