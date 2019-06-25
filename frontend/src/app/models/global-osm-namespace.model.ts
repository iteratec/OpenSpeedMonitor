export interface GlobalOsmNamespace {
  i18n: {
    lang: 'de'|'en'
  },
  user: {
    loggedIn: boolean
  },
  postLoader: {
    loadJavascript(url: string, name: string): void;
    onLoaded(dependencies, callback): void;
  },
  ChartModules: {
    Aggregation(selector: string): void;
  },
  assetPaths: {
    aggregationChart: string
  }
}
