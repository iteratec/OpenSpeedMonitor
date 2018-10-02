import {Application} from "./application.model";

export interface ApplicationList {
  isLoading: boolean;
  applications: Application[];
}
