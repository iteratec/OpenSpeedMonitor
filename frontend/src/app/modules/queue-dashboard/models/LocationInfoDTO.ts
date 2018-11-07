export interface LocationInfoDTO {
  id: string
  lastHealthCheckDate: string
  label: string
  agents: number
  jobs: number

  eventResultsLastHour: number
  jobResultsLastHour: number
  errorsLastHour: number

  jobsNextHour: number
  eventsNextHour: number

  executingJobs: Array<Array<any>>
  pendingJobs: number
  runningJobs: number
}
