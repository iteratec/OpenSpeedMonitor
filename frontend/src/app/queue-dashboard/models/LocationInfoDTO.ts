export interface LocationInfoDTO {
  id: string
  lastHealthCheckDate: string
  agents: number
  label: string
  jobs: number

  eventResultLastHour: number
  jobResultsLastHour: number
  errorsLastHour: number

  jobsNextHour: number
  eventsNextHour: number

  pendingJobs: number
  runningJobs: number
}
