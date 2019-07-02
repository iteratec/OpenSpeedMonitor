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
    AggregationData(selector: string): void;
    GuiHandling:{
      aggregation(): void;
    }
  },
  ChartColorProvider(): void;
  assetPaths: {
    aggregationChart: string,
    aggregationGuiHandling: string
  }
}
