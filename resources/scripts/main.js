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
        <div className="ui grid">
          <div className="row"><br/><br/><br/><br/><br/><br/><br/></div>
          <div className="row">
              <div className="four wide column"></div>
              <div className="eight wide column">
                <h1 style={{textAlign:"center",color:'crimson',fontFamily:'Cochin',fontWeight:'bold',fontSize: 50}}>Mini Google</h1>
                <div className="row"><br/></div>
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
                <div className="row"><br/></div>
                <div
                  dangerouslySetInnerHTML={this.getRawMarkup()}
                />
              </div>
          </div>
      </div>
    );
  }
}

ReactDOM.render(<MarkdownEditor />, document.getElementById('root'));

$('.ui.search')
  .search({
    type          : 'category',
    apiSettings: {
      onResponse: function(githubResponse) {
              var
                response = {
                  results : {}
                }
              ;
              response.results["Github"] = {
                name    : "Github",
                results : []
              };
              // translate GitHub API response to work with search
              $.each(githubResponse.items, function(index, item) {
                // add result to category
                response.results["Github"].results.push({
                  title       : item.name,
                  description : item.description,
                  url         : item.html_url
                });
              });
              return response;
            },
            url: '//api.github.com/search/repositories?q={query}'
    },
    minCharacters : 3
  })
;