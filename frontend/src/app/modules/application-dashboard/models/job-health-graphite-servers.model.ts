import {GraphiteServer} from "./graphite-server.model";

export interface JobHealthGraphiteServersDTO {
  jobHealthGraphiteServers: GraphiteServer[];
}

export class JobHealthGraphiteServers implements JobHealthGraphiteServersDTO {
  jobHealthGraphiteServers: GraphiteServer[];

  constructor(dto: JobHealthGraphiteServersDTO) {
    this.jobHealthGraphiteServers = dto.jobHealthGraphiteServers;
  }
}
