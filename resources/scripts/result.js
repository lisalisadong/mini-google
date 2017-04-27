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
            <div className="row"></div>
            <div className="row">
            <div className="two wide column" style={{textAlign:'right'}}>
                <div className="ui red basic medium label" style={{color:'crimson',fontFamily:'Cochin',fontWeight:'bold',fontSize: 16}}><a href="/">Mini Google</a></div>
            </div>
            <div className="six wide column">
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
            <div dangerouslySetInnerHTML={this.getRawMarkup()} />
            <div className="eight wide column"></div>
        </div>

        <div className="ui divider"></div>
        <div className="row">
        <div className="two wide column"></div>
        <div className="eight wide column">
            <div className="ui fluid link card" href="#">
                <div className="content">
                    <i className="right floated like thumbs up icon"></i>
                    <i className="right floated star thumbs down icon"></i>
                    <div className="header" style={{color:'crimson',fontFamily:'Cochin',fontSize: 20}}>This is an example</div>
                    <div className="meta" style={{color:'steelblue',fontFamily:'Cochin',fontSize: 15}}>
                        <span className="category">https://this/is/an/example/link</span>
                    </div>
                    <div className="description" style={{color:'grey',fontFamily:'Cochin',fontSize: 15}}>
                        <text>Apr 26, 2017 - </text>
                        <text>Cute dogs come in a variety of shapes and sizes. Some cute dogs are cute for their adorable faces, others for their tiny stature, and even others for their massive size.</text>
                    </div>
                </div>
            </div>

            <div className="ui fluid link card" href="#">
                <div className="content">
                    <i className="right floated like thumbs up icon"></i>
                    <i className="right floated star thumbs down icon"></i>
                    <div className="header" style={{color:'crimson',fontFamily:'Cochin',fontSize: 20}}>This is an example</div>
                    <div className="meta" style={{color:'steelblue',fontFamily:'Cochin',fontSize: 15}}>
                        <span className="category">https://this/is/an/example/link</span>
                    </div>
                    <div className="description" style={{color:'grey',fontFamily:'Cochin',fontSize: 15}}>
                        <text>Apr 26, 2017 - </text>
                        <text>Cute dogs come in a variety of shapes and sizes. Some cute dogs are cute for their adorable faces, others for their tiny stature, and even others for their massive size.</text>
                    </div>
                </div>
            </div>

            <div className="ui fluid link card" href="#">
                <div className="content">
                    <i className="right floated like thumbs up icon"></i>
                    <i className="right floated star thumbs down icon"></i>
                    <div className="header" style={{color:'crimson',fontFamily:'Cochin',fontSize: 20}}>This is an example</div>
                    <div className="meta" style={{color:'steelblue',fontFamily:'Cochin',fontSize: 15}}>
                        <span className="category">https://this/is/an/example/link</span>
                    </div>
                    <div className="description" style={{color:'grey',fontFamily:'Cochin',fontSize: 15}}>
                        <text>Apr 26, 2017 - </text>
                        <text>Cute dogs come in a variety of shapes and sizes. Some cute dogs are cute for their adorable faces, others for their tiny stature, and even others for their massive size.</text>
                    </div>
                </div>
            </div>

            <div className="ui fluid link card" href="#">
                <div className="content">
                    <i className="right floated like thumbs up icon"></i>
                    <i className="right floated star thumbs down icon"></i>
                    <div className="header" style={{color:'crimson',fontFamily:'Cochin',fontSize: 20}}>This is an example</div>
                    <div className="meta" style={{color:'steelblue',fontFamily:'Cochin',fontSize: 15}}>
                        <span className="category">https://this/is/an/example/link</span>
                    </div>
                    <div className="description" style={{color:'grey',fontFamily:'Cochin',fontSize: 15}}>
                        <text>Apr 26, 2017 - </text>
                        <text>Cute dogs come in a variety of shapes and sizes. Some cute dogs are cute for their adorable faces, others for their tiny stature, and even others for their massive size.</text>
                    </div>
                </div>
            </div>

            <div className="ui fluid link card" href="#">
                <div className="content">
                    <i className="right floated like thumbs up icon"></i>
                    <i className="right floated star thumbs down icon"></i>
                    <div className="header" style={{color:'crimson',fontFamily:'Cochin',fontSize: 20}}>This is an example</div>
                    <div className="meta" style={{color:'steelblue',fontFamily:'Cochin',fontSize: 15}}>
                        <span className="category">https://this/is/an/example/link</span>
                    </div>
                    <div className="description" style={{color:'grey',fontFamily:'Cochin',fontSize: 15}}>
                        <text>Apr 26, 2017 - </text>
                        <text>Cute dogs come in a variety of shapes and sizes. Some cute dogs are cute for their adorable faces, others for their tiny stature, and even others for their massive size.</text>
                    </div>
                </div>
            </div>

            <div className="ui fluid link card" href="#">
                <div className="content">
                    <i className="right floated like thumbs up icon"></i>
                    <i className="right floated star thumbs down icon"></i>
                    <div className="header" style={{color:'crimson',fontFamily:'Cochin',fontSize: 20}}>This is an example</div>
                    <div className="meta" style={{color:'steelblue',fontFamily:'Cochin',fontSize: 15}}>
                        <span className="category">https://this/is/an/example/link</span>
                    </div>
                    <div className="description" style={{color:'grey',fontFamily:'Cochin',fontSize: 15}}>
                        <text>Apr 26, 2017 - </text>
                        <text>Cute dogs come in a variety of shapes and sizes. Some cute dogs are cute for their adorable faces, others for their tiny stature, and even others for their massive size.</text>
                    </div>
                </div>
            </div>

            <div className="ui fluid link card" href="#">
                <div className="content">
                    <i className="right floated like thumbs up icon"></i>
                    <i className="right floated star thumbs down icon"></i>
                    <div className="header" style={{color:'crimson',fontFamily:'Cochin',fontSize: 20}}>This is an example</div>
                    <div className="meta" style={{color:'steelblue',fontFamily:'Cochin',fontSize: 15}}>
                        <span className="category">https://this/is/an/example/link</span>
                    </div>
                    <div className="description" style={{color:'grey',fontFamily:'Cochin',fontSize: 15}}>
                        <text>Apr 26, 2017 - </text>
                        <text>Cute dogs come in a variety of shapes and sizes. Some cute dogs are cute for their adorable faces, others for their tiny stature, and even others for their massive size.</text>
                    </div>
                </div>
            </div>

            <div className="ui fluid link card" href="#">
                <div className="content">
                    <i className="right floated like thumbs up icon"></i>
                    <i className="right floated star thumbs down icon"></i>
                    <div className="header" style={{color:'crimson',fontFamily:'Cochin',fontSize: 20}}>This is an example</div>
                    <div className="meta" style={{color:'steelblue',fontFamily:'Cochin',fontSize: 15}}>
                        <span className="category">https://this/is/an/example/link</span>
                    </div>
                    <div className="description" style={{color:'grey',fontFamily:'Cochin',fontSize: 15}}>
                        <text>Apr 26, 2017 - </text>
                        <text>Cute dogs come in a variety of shapes and sizes. Some cute dogs are cute for their adorable faces, others for their tiny stature, and even others for their massive size.</text>
                    </div>
                </div>
            </div>

            <div className="ui fluid link card" href="#">
                <div className="content">
                    <i className="right floated like thumbs up icon"></i>
                    <i className="right floated star thumbs down icon"></i>
                    <div className="header" style={{color:'crimson',fontFamily:'Cochin',fontSize: 20}}>This is an example</div>
                    <div className="meta" style={{color:'steelblue',fontFamily:'Cochin',fontSize: 15}}>
                        <span className="category">https://this/is/an/example/link</span>
                    </div>
                    <div className="description" style={{color:'grey',fontFamily:'Cochin',fontSize: 15}}>
                        <text>Apr 26, 2017 - </text>
                        <text>Cute dogs come in a variety of shapes and sizes. Some cute dogs are cute for their adorable faces, others for their tiny stature, and even others for their massive size.</text>
                    </div>
                </div>
            </div>

            <div className="ui fluid link card" href="#">
                <div className="content">
                    <i className="right floated like thumbs up icon"></i>
                    <i className="right floated star thumbs down icon"></i>
                    <div className="header" style={{color:'crimson',fontFamily:'Cochin',fontSize: 20}}>This is an example</div>
                    <div className="meta" style={{color:'steelblue',fontFamily:'Cochin',fontSize: 15}}>
                        <span className="category">https://this/is/an/example/link</span>
                    </div>
                    <div className="description" style={{color:'grey',fontFamily:'Cochin',fontSize: 15}}>
                        <text>Apr 26, 2017 - </text>
                        <text>Cute dogs come in a variety of shapes and sizes. Some cute dogs are cute for their adorable faces, others for their tiny stature, and even others for their massive size.</text>
                    </div>
                </div>
            </div>

            <div className="row"><br/></div>
            <div className="row" style={{textAlign:'center'}}>
                <a className="ui red circular label">prev</a>
                <a className="ui orange circular label">1</a>
                <a className="ui yellow circular label">2</a>
                <a className="ui olive circular label">3</a>
                <a className="ui green circular label">4</a>
                <a className="ui teal circular label">5</a>
                <a className="ui blue circular label">6</a>
                <a className="ui violet circular label">7</a>
                <a className="ui purple circular label">8</a>
                <a className="ui pink circular label">9</a>
                <a className="ui grey circular label">10</a>
                <a className="ui black circular label">next</a>
            </div>
        </div>
        <div className="five wide column">
            <div className="ui fluid link card" href="#">
                <div className="content">
                    <div className="header" style={{color:'steelblue',fontFamily:'Cochin',fontSize: 20}}>API Content</div>
                    <div className="meta" style={{color:'crimson',fontFamily:'Cochin',fontSize: 15}}>
                        <span className="category">source</span>
                    </div>
                    <div className="description" style={{color:'grey',fontFamily:'Cochin',fontSize: 15}}>
                        <p>API content will be displayed here</p>
                        <p>- weather</p>
                        <p>- wikipedia</p>
                        <p>- shopping</p>
                    </div>
                </div>
            </div>
        </div>
        <div className="one wide column"></div>
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