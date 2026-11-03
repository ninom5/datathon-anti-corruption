import {Route, Switch} from 'wouter';
import {routes} from '@/routes';
import {BuyerPage, ContractPage, ContractsPage, HomePage, IndicatorPage, NotFoundPage, SourcesPage} from "@/pages";
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
                <Route component={NotFoundPage}/>
            </Switch>
        </AppLayout>
    );
};
