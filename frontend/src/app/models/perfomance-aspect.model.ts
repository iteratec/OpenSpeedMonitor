import {SelectableMeasurand} from "./measurand.model";
import {BrowserInfoDto} from "./browser.model";


export interface PerformanceAspect {
  id: number
  pageId: number
  jobGroupId: number
  browserId: number
  measurand: SelectableMeasurand
  performanceAspectType: PerformanceAspectType
  persistent: boolean
}

export type ExtendedPerformanceAspect = PerformanceAspect & BrowserInfoDto

export interface PerformanceAspectType {
  name: string
  icon: string
}
