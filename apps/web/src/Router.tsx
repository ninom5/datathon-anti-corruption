import {Route, Switch} from 'wouter';
import {routes} from '@/routes';
import {BuyerPage, ComparisonPage, ContractPage, ContractsPage, FeedbackPage, HomePage, IndicatorPage, MapPage, MethodologyPage, NotFoundPage, SourcesPage} from "@/pages";
import {AppLayout} from "@/layouts";

export const Router = () => {
    return (
        <AppLayout>
            <Switch>
                <Route path={routes.HOME} component={HomePage}/>
                <Route path={routes.CONTRACTS} component={ContractsPage}/>
                <Route path={routes.CONTRACT} component={ContractPage}/>
                <Route path={routes.BUYER} component={BuyerPage}/>
                <Route path={routes.INDICATOR} component={IndicatorPage}/>
                <Route path={routes.SOURCES} component={SourcesPage}/>
                <Route path={routes.MAP} component={MapPage}/>
                <Route path={routes.COMPARISON} component={ComparisonPage}/>
                <Route path={routes.METHODOLOGY} component={MethodologyPage}/>
                <Route path={routes.FEEDBACK} component={FeedbackPage}/>
                <Route component={NotFoundPage}/>
            </Switch>
        </AppLayout>
    );
};
