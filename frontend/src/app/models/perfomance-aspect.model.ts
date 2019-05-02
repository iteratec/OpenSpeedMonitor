import {SelectableMeasurand} from "./measurand.model";


export interface PerformanceAspect {
  id: number
  pageId: number
  jobGroupId: number
  browserId: number
  measurand: SelectableMeasurand
  performanceAspectType: string
}
