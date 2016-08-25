import React, { Component, PropTypes } from 'react';
import { connect } from 'react-redux';

import ReactCSSTransitionGroup from 'react-addons-css-transition-group';

import {
    dropdownRequest
} from '../../actions/AppActions';

class List extends Component {
    constructor(props) {
        super(props);
        this.state = {
            list: []
        }
    }
    componentDidMount() {
        const {selected} = this.props;
        if(selected){
            this.handleSelect(selected, true);
        }
    }
    handleBlur = (e) => {
        this.dropdown.classList.remove("input-dropdown-focused");
    }
    handleFocus = (e) => {
        e.preventDefault();
        const {properties, dispatch, dataId} = this.props;
        this.setState({loading: true});
        dispatch(dropdownRequest(143, properties[0].field, dataId)).then((res) => {
            this.setState({list: res.data, loading: false});
        });
        this.dropdown.classList.add("input-dropdown-focused");
    }
    handleChange = (e) => {
        e.preventDefault();
        this.handleBlur();
    }
    handleSelect = (option, init = false) => {
        this.inputSearch.value = option[Object.keys(option)[0]];
        if(!init){
            this.props.onChange(option);
        }
        this.handleBlur();
    }
    renderOptions = () => {
        return this.state.list.map((option, index) => (
                <div key={index} className={"input-dropdown-list-option"} onClick={() => this.handleSelect(option)}>
                    <p className="input-dropdown-item-title">{option[Object.keys(option)[0]]}</p>
                </div>
            )
        )
    }
    render() {
        const {list, rank,readonly, value} = this.props;
        return (
            <div
                tabIndex="0"
                onFocus={()=>this.inputSearch.focus()}
                ref={(c) => this.dropdown = c}
                onBlur={this.handleBlur}
                className={"input-dropdown-container"}
            >
                <div className={"input-dropdown input-block input-readonly input-" + (rank ? rank : "secondary")}>
                    <div className="input-editable input-dropdown-focused">
                        <input
                            type="text"
                            className="input-field font-weight-bold"
                            readOnly="readonly"
                            placeholder={this.props.defaultValue}
                            onFocus={this.handleFocus}
                            onChange={this.handleChange}
                            ref={(c) => this.inputSearch = c}
                            disabled={readonly}
                        />
                    </div>
                    <div className="input-icon">
                        <i className="meta-icon-down-1 input-icon-sm"/>
                    </div>
                </div>
                <div className="input-dropdown-list">
                    {(this.state.list.length === 0 && this.state.loading === false) && (
                        <div className="input-dropdown-list-header">
                            There is no choice available
                        </div>
                    )}
                    {(this.state.loading && this.state.list.length === 0) && (
                        <div className="input-dropdown-list-header">
                            <ReactCSSTransitionGroup transitionName="rotate" transitionEnterTimeout={1000} transitionLeaveTimeout={1000}>
                                <div className="rotate icon-rotate">
                                    <i className="meta-icon-settings"/>
                                </div>
                            </ReactCSSTransitionGroup>
                        </div>
                    )}
                    {this.renderOptions()}
                </div>
            </div>
        )
    }
}

List.propTypes = {
    dispatch: PropTypes.func.isRequired
};

function mapStateToProps(state) {
    return {
    }
}

List = connect(mapStateToProps)(List)

export default List
