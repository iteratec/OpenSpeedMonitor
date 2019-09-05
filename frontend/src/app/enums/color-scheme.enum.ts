export enum COLOR_SCHEME {
  TEAL                  = '#7fffd4', // teal
  GREY                  = '#aeaeae', // grey
  DIRTY_GREEN           = '#00ced1', // dirty green
  ORANGE                = '#fcce1c', // orange
  BROWN                 = '#c90707', // brown
  LIGHT_CYAN            = '#0edbec', // light blue/green
  PINK                  = '#FFC0CB', // pink
  BRIGHT_BROWN          = '#d01265', // bright brown
  DARK_GREEN            = '#75ad3e', // dark green
  LIGHT_BLUE            = '#c9c9ff', // light blue
  LIGHT_GREEN           = '#00dd2f', // light green
  DARK_BLUE             = '#751ec3', // dark blue
  RED                   = '#ff0000', // red
  BLACK                 = '#0d233a', // black
  YELLOW                = '#ffdb00', // yellow  
  HIGHCHARTS_YELLOW     = '#ffdc08', // highcharts yellow
  HIGHCHARTS_PURPLE     = '#492970', // highcharts purple
  HIGHCHARTS_LIGHT_BLUE = '#77a1e5', // highcharts light blue
  HIGHCHARTS_ORANGE     = '#f28f43', // highcharts orange
  HIGHCHARTS_RED        = '#910000', // highcharts red
  HIGHCHARTS_BLACK      = '#0d233a', // highcharts black
  HIGHCHARTS_BLUE       = '#2f7ed8', // highcharts blue
  HIGHCHARTS_GREEN      = '#8bbc21' // highcharts green
}

export function getColorScheme(): string[] {
  return Object.keys(COLOR_SCHEME)
               .filter((key: any) => isNaN(key))
               .map((key: string) => COLOR_SCHEME[key])
               .reverse();
}
