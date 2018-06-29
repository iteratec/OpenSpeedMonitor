import { ApplicationDashboardModule } from './application-dashboard.module';

describe('ApplicationDashboardModule', () => {
  let applicationDashboardModule: ApplicationDashboardModule;

  beforeEach(() => {
    applicationDashboardModule = new ApplicationDashboardModule();
  });

  it('should create an instance', () => {
    expect(applicationDashboardModule).toBeTruthy();
  });
});
