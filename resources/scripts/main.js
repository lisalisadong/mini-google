class MarkdownEditor extends React.Component {
  constructor(props) {
    super(props);
    this.handleChange = this.handleChange.bind(this);
    this.state = {value: ''};
  }

  handleChange(e) {
    this.setState({value: e.target.value});
  }

  getRawMarkup() {
    var md = new Remarkable();
    return { __html: md.render(this.state.value) };
  }

  render() {
    return (
        <div>
          <div className="row"><br/><br/><br/><br/><br/><br/><br/></div>
          <div className="col-sm-3"></div>
          <div className="col-sm-6">
            <h1 style={{textAlign:"center",color:'crimson',fontFamily:'Cochin',fontWeight:'bold',fontSize: 50}}>Mini Google</h1>
            <div className="row"><br/></div>
            <form action="/search" method="get">
                <input className="form-control" type="search" autoFocus="true" name="query"
                  onChange={this.handleChange}
                  defaultValue={this.state.value} />
            </form>
            <div className="row"><br/></div>
            <div
              dangerouslySetInnerHTML={this.getRawMarkup()}
            />
          </div>
      </div>
    );
  }
}

ReactDOM.render(<MarkdownEditor />, document.getElementById('root'));