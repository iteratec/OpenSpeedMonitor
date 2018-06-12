import { SetupDashboardModule } from './setup-dashboard.module';

describe('SetupDashboardModule', () => {
  let setupDashboardModule: SetupDashboardModule;

  beforeEach(() => {
    setupDashboardModule = new SetupDashboardModule();
  });

  it('should create an instance', () => {
    expect(setupDashboardModule).toBeTruthy();
  });
});
