class MarkdownEditor extends React.Component {
    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.state = {value: ''};
}

handleChange(e) {
    this.setState({value: e.target.value});
}

render() {
    return (
        <div className="ui grid">
            <div className="row"></div>
            <div className="row">
                <div className="two wide column" style={{textAlign:'right'}}>
                    <div className="ui red basic medium label" style={{color:'crimson',fontFamily:'Cochin',fontWeight:'bold',fontSize: 16}}><a href="/">Mini Google</a></div>
                </div>
                <div className="eight wide column">
                    <form action="/search" method="get">
                        <div className="ui fluid category search">
                            <div className="ui fluid icon input">
                                <input className="prompt" type="text" autoFocus="true" name="query"
                                  onChange={this.handleChange}
                                  defaultValue={this.state.value} />
                                <i aria-hidden="true" className="search icon" style={{color:'steelblue'}}></i>
                            </div>
                            <div className="results"></div>
                        </div>
                    </form>
                </div>
                <div className="six wide column"></div>
            </div>
            <div className="ui divider"></div>
        </div>
    );
  }
}

ReactDOM.render(<MarkdownEditor />, document.getElementById('root'));

$('.ui.search')
  .search({
    apiSettings: {
      onResponse: function(githubResponse) {
              var
                response = {
                  results : []
                }
              ;
              // translate GitHub API response to work with search
              $.each(githubResponse.predictions, function(index, item) {
                // add result to category
                response.results.push({
                  description : item,
                  url         : '/search?query=' + item
                });
              });
              return response;
            },
            url: '/api/predict?q={query}'
    },
    minCharacters : 3
  })
;