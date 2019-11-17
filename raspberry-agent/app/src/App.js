import React, { Component } from 'react';
import './App.css';
import Home from './Home';
import { BrowserRouter as Router, Route, Switch } from 'react-router-dom';
import ConfigList from './ConfigList';
import ConfigEdit from './ConfigEdit';
import TestConnection from './TestConnection';


class App extends Component {
  render() {
    return (
      <Router>
        <Switch>
          <Route path='/' exact={true} component={Home}/>
          <Route path='/configs' exact={true} component={ConfigList}/>
          <Route path='/configs/:id' exact={true} component={ConfigEdit}/>
          <Route path='/testConnection' exact={true} component={TestConnection}/>
        </Switch>
      </Router>
    )
  }
}

export default App;
